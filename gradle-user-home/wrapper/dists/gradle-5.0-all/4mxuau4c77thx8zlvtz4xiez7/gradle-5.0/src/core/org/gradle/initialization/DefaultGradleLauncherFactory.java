/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.initialization;

import com.google.common.collect.ImmutableList;
import org.gradle.BuildResult;
import org.gradle.StartParameter;
import org.gradle.api.internal.BuildDefinition;
import org.gradle.api.internal.ExceptionAnalyser;
import org.gradle.api.internal.GradleInternal;
import org.gradle.api.internal.tasks.execution.statistics.TaskExecutionStatisticsEventAdapter;
import org.gradle.api.logging.Logging;
import org.gradle.api.logging.configuration.ShowStacktrace;
import org.gradle.composite.internal.IncludedBuildControllers;
import org.gradle.configuration.BuildConfigurer;
import org.gradle.deployment.internal.DefaultDeploymentRegistry;
import org.gradle.execution.BuildConfigurationActionExecuter;
import org.gradle.execution.BuildExecuter;
import org.gradle.internal.InternalBuildAdapter;
import org.gradle.internal.build.BuildState;
import org.gradle.internal.build.NestedBuildState;
import org.gradle.internal.build.RootBuildState;
import org.gradle.internal.buildevents.BuildLogger;
import org.gradle.internal.buildevents.BuildStartedTime;
import org.gradle.internal.buildevents.TaskExecutionStatisticsReporter;
import org.gradle.internal.classpath.ClassPath;
import org.gradle.internal.concurrent.Stoppable;
import org.gradle.internal.event.ListenerManager;
import org.gradle.internal.featurelifecycle.DeprecatedUsageBuildOperationProgressBroadaster;
import org.gradle.internal.featurelifecycle.LoggingDeprecatedFeatureHandler;
import org.gradle.internal.featurelifecycle.ScriptUsageLocationReporter;
import org.gradle.internal.logging.text.StyledTextOutputFactory;
import org.gradle.internal.operations.BuildOperationExecutor;
import org.gradle.internal.reflect.Instantiator;
import org.gradle.internal.service.ServiceRegistry;
import org.gradle.internal.service.scopes.BuildScopeServices;
import org.gradle.internal.service.scopes.BuildSessionScopeServices;
import org.gradle.internal.service.scopes.BuildTreeScopeServices;
import org.gradle.internal.service.scopes.CrossBuildSessionScopeServices;
import org.gradle.internal.service.scopes.GradleUserHomeScopeServiceRegistry;
import org.gradle.internal.service.scopes.ServiceRegistryFactory;
import org.gradle.internal.time.Clock;
import org.gradle.internal.time.Time;
import org.gradle.invocation.DefaultGradle;
import org.gradle.profile.ProfileEventAdapter;
import org.gradle.profile.ReportGeneratingProfileListener;
import org.gradle.util.DeprecationLogger;

import javax.annotation.Nullable;
import java.util.List;

public class DefaultGradleLauncherFactory implements GradleLauncherFactory {
    private final GradleUserHomeScopeServiceRegistry userHomeDirServiceRegistry;
    private final CrossBuildSessionScopeServices crossBuildSessionScopeServices;
    private DefaultGradleLauncher rootBuild;

    public DefaultGradleLauncherFactory(
        GradleUserHomeScopeServiceRegistry userHomeDirServiceRegistry,
        CrossBuildSessionScopeServices crossBuildSessionScopeServices
    ) {
        this.userHomeDirServiceRegistry = userHomeDirServiceRegistry;
        this.crossBuildSessionScopeServices = crossBuildSessionScopeServices;
    }

    private GradleLauncher createChildInstance(BuildDefinition buildDefinition, BuildState build, GradleLauncher parent, BuildTreeScopeServices buildTreeScopeServices, List<?> servicesToStop) {
        ServiceRegistry services = parent.getGradle().getServices();
        BuildRequestMetaData requestMetaData = new DefaultBuildRequestMetaData(services.get(BuildClientMetaData.class));
        BuildCancellationToken cancellationToken = services.get(BuildCancellationToken.class);
        BuildEventConsumer buildEventConsumer = services.get(BuildEventConsumer.class);
        return doNewInstance(buildDefinition, build, parent, cancellationToken, requestMetaData, buildEventConsumer, buildTreeScopeServices, servicesToStop);
    }

    @Override
    public GradleLauncher newInstance(BuildDefinition buildDefinition, RootBuildState build, BuildRequestContext requestContext, ServiceRegistry parentRegistry) {
        // This should only be used for top-level builds
        if (rootBuild != null) {
            throw new IllegalStateException("Cannot have a current root build");
        }

        if (!(parentRegistry instanceof BuildTreeScopeServices)) {
            throw new IllegalArgumentException("Service registry must be of build-tree scope");
        }
        BuildTreeScopeServices buildTreeScopeServices = (BuildTreeScopeServices) parentRegistry;

        DefaultGradleLauncher launcher = doNewInstance(buildDefinition, build, null,
            requestContext.getCancellationToken(),
            requestContext, requestContext.getEventConsumer(), buildTreeScopeServices,
            ImmutableList.of(new Stoppable() {
                @Override
                public void stop() {
                    rootBuild = null;
                }
            }));
        rootBuild = launcher;

        final DefaultDeploymentRegistry deploymentRegistry = parentRegistry.get(DefaultDeploymentRegistry.class);
        launcher.getGradle().addBuildListener(new InternalBuildAdapter() {
            @Override
            public void buildFinished(BuildResult result) {
                deploymentRegistry.buildFinished(result);
            }
        });

        return launcher;
    }

    private DefaultGradleLauncher doNewInstance(BuildDefinition buildDefinition,
                                                BuildState build,
                                                @Nullable GradleLauncher parent,
                                                BuildCancellationToken cancellationToken,
                                                BuildRequestMetaData requestMetaData,
                                                BuildEventConsumer buildEventConsumer,
                                                final BuildTreeScopeServices buildTreeScopeServices,
                                                List<?> servicesToStop) {
        BuildScopeServices serviceRegistry = new BuildScopeServices(buildTreeScopeServices);
        serviceRegistry.add(BuildDefinition.class, buildDefinition);
        serviceRegistry.add(BuildRequestMetaData.class, requestMetaData);
        serviceRegistry.add(BuildClientMetaData.class, requestMetaData.getClient());
        serviceRegistry.add(BuildEventConsumer.class, buildEventConsumer);
        serviceRegistry.add(BuildCancellationToken.class, cancellationToken);
        serviceRegistry.add(BuildState.class, build);
        NestedBuildFactoryImpl nestedBuildFactory = new NestedBuildFactoryImpl(buildTreeScopeServices);
        serviceRegistry.add(NestedBuildFactory.class, nestedBuildFactory);

        StartParameter startParameter = buildDefinition.getStartParameter();
        ListenerManager listenerManager = serviceRegistry.get(ListenerManager.class);

        Clock clock = serviceRegistry.get(Clock.class);
        if (parent == null) {
            BuildStartedTime buildStartedTime = serviceRegistry.get(BuildStartedTime.class);
            listenerManager.useLogger(new BuildLogger(Logging.getLogger(BuildLogger.class), serviceRegistry.get(StyledTextOutputFactory.class), startParameter, requestMetaData, buildStartedTime, clock));
        }

        listenerManager.addListener(serviceRegistry.get(TaskExecutionStatisticsEventAdapter.class));
        listenerManager.addListener(new TaskExecutionStatisticsReporter(serviceRegistry.get(StyledTextOutputFactory.class)));

        if (startParameter.isProfile()) {
            listenerManager.addListener(serviceRegistry.get(ProfileEventAdapter.class));
            listenerManager.addListener(new ReportGeneratingProfileListener(serviceRegistry.get(StyledTextOutputFactory.class)));
        }

        ScriptUsageLocationReporter usageLocationReporter = new ScriptUsageLocationReporter();
        listenerManager.addListener(usageLocationReporter);
        ShowStacktrace showStacktrace = startParameter.getShowStacktrace();
        switch (showStacktrace) {
            case ALWAYS:
            case ALWAYS_FULL:
                LoggingDeprecatedFeatureHandler.setTraceLoggingEnabled(true);
                break;
            default:
                LoggingDeprecatedFeatureHandler.setTraceLoggingEnabled(false);
        }

        BuildOperationExecutor buildOperationExecutor = serviceRegistry.get(BuildOperationExecutor.class);
        DeprecatedUsageBuildOperationProgressBroadaster deprecationWarningBuildOperationProgressBroadaster = serviceRegistry.get(DeprecatedUsageBuildOperationProgressBroadaster.class);
        DeprecationLogger.init(usageLocationReporter, startParameter.getWarningMode(), deprecationWarningBuildOperationProgressBroadaster);

        SettingsLoaderFactory settingsLoaderFactory = serviceRegistry.get(SettingsLoaderFactory.class);
        SettingsLoader settingsLoader = parent != null ? settingsLoaderFactory.forNestedBuild() : settingsLoaderFactory.forTopLevelBuild();
        GradleInternal parentBuild = parent == null ? null : parent.getGradle();

        GradleInternal gradle = serviceRegistry.get(Instantiator.class).newInstance(DefaultGradle.class, parentBuild, startParameter, serviceRegistry.get(ServiceRegistryFactory.class));

        IncludedBuildControllers includedBuildControllers;
        if (parent == null) {
            includedBuildControllers = buildTreeScopeServices.get(IncludedBuildControllers.class);
        } else {
            includedBuildControllers = IncludedBuildControllers.EMPTY;
        }
        DefaultGradleLauncher gradleLauncher = new DefaultGradleLauncher(
            gradle,
            serviceRegistry.get(InitScriptHandler.class),
            settingsLoader,
            serviceRegistry.get(BuildLoader.class),
            serviceRegistry.get(BuildConfigurer.class),
            serviceRegistry.get(ExceptionAnalyser.class),
            gradle.getBuildListenerBroadcaster(),
            listenerManager.getBroadcaster(ModelConfigurationListener.class),
            listenerManager.getBroadcaster(BuildCompletionListener.class),
            buildOperationExecutor,
            gradle.getServices().get(BuildConfigurationActionExecuter.class),
            gradle.getServices().get(BuildExecuter.class),
            serviceRegistry,
            servicesToStop,
            includedBuildControllers,
            buildDefinition.getFromBuild()
        );
        nestedBuildFactory.setParent(gradleLauncher);
        nestedBuildFactory.setBuildCancellationToken(cancellationToken);
        return gradleLauncher;
    }

    private class NestedBuildFactoryImpl implements NestedBuildFactory {
        private final BuildTreeScopeServices buildTreeScopeServices;
        private DefaultGradleLauncher parent;
        private BuildCancellationToken buildCancellationToken;

        public NestedBuildFactoryImpl(BuildTreeScopeServices buildTreeScopeServices) {
            this.buildTreeScopeServices = buildTreeScopeServices;
        }

        @Override
        public GradleLauncher nestedInstance(BuildDefinition buildDefinition, NestedBuildState build) {
            return createChildInstance(buildDefinition, build, parent, buildTreeScopeServices, ImmutableList.of());
        }

        @Override
        public GradleLauncher nestedBuildTree(BuildDefinition buildDefinition, NestedBuildState build) {
            StartParameter startParameter = buildDefinition.getStartParameter();
            final ServiceRegistry userHomeServices = userHomeDirServiceRegistry.getServicesFor(startParameter.getGradleUserHomeDir());
            BuildRequestMetaData buildRequestMetaData = new DefaultBuildRequestMetaData(Time.currentTimeMillis());
            BuildSessionScopeServices sessionScopeServices = new BuildSessionScopeServices(userHomeServices, crossBuildSessionScopeServices, startParameter, buildRequestMetaData, ClassPath.EMPTY, buildCancellationToken);
            BuildTreeScopeServices buildTreeScopeServices = new BuildTreeScopeServices(sessionScopeServices);
            return createChildInstance(buildDefinition, build, parent, buildTreeScopeServices, ImmutableList.of(buildTreeScopeServices, sessionScopeServices, new Stoppable() {
                @Override
                public void stop() {
                    userHomeDirServiceRegistry.release(userHomeServices);
                }
            }));
        }

        private void setParent(DefaultGradleLauncher parent) {
            this.parent = parent;
        }

        private void setBuildCancellationToken(BuildCancellationToken buildCancellationToken) {
            this.buildCancellationToken = buildCancellationToken;
        }
    }
}
