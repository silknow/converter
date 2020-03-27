/*
 * Copyright 2011 the original author or authors.
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
package org.gradle.api.internal.artifacts.repositories;

import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.api.artifacts.repositories.AuthenticationContainer;
import org.gradle.api.artifacts.repositories.PasswordCredentials;
import org.gradle.api.credentials.Credentials;
import org.gradle.api.model.ObjectFactory;
import org.gradle.authentication.Authentication;
import org.gradle.internal.Cast;
import org.gradle.internal.artifacts.repositories.AuthenticationSupportedInternal;
import org.gradle.internal.authentication.AuthenticationInternal;
import org.gradle.internal.reflect.Instantiator;
import org.gradle.util.CollectionUtils;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public abstract class AbstractAuthenticationSupportedRepository extends AbstractResolutionAwareArtifactRepository implements AuthenticationSupportedInternal {
    private final AuthenticationSupporter delegate;

    AbstractAuthenticationSupportedRepository(Instantiator instantiator, AuthenticationContainer authenticationContainer, ObjectFactory objectFactory) {
        super(objectFactory);
        this.delegate = new AuthenticationSupporter(instantiator, authenticationContainer);
    }

    @Override
    public PasswordCredentials getCredentials() {
        invalidateDescriptor();
        return delegate.getCredentials();
    }

    @Override
    public <T extends Credentials> T getCredentials(Class<T> credentialsType) {
        invalidateDescriptor();
        return delegate.getCredentials(credentialsType);
    }

    @Nullable
    @Override
    public Credentials getConfiguredCredentials() {
        return delegate.getConfiguredCredentials();
    }

    @Override
    public void setConfiguredCredentials(Credentials credentials) {
        invalidateDescriptor();
        delegate.setConfiguredCredentials(credentials);
    }

    @Override
    public void credentials(Action<? super PasswordCredentials> action) {
        invalidateDescriptor();
        delegate.credentials(action);
    }

    @Override
    public <T extends Credentials> void credentials(Class<T> credentialsType, Action<? super T> action) throws IllegalStateException {
        invalidateDescriptor();
        delegate.credentials(credentialsType, action);
    }

    @Override
    public void authentication(Action<? super AuthenticationContainer> action) {
        invalidateDescriptor();
        delegate.authentication(action);
    }

    @Override
    public AuthenticationContainer getAuthentication() {
        invalidateDescriptor();
        return delegate.getAuthentication();
    }

    @Override
    public Collection<Authentication> getConfiguredAuthentication() {
        return delegate.getConfiguredAuthentication();
    }

    List<String> getAuthenticationSchemes() {
        return CollectionUtils.collect(getConfiguredAuthentication(), new Transformer<String, Authentication>() {
            @Override
            public String transform(Authentication authentication) {
                return Cast.cast(AuthenticationInternal.class, authentication).getType().getSimpleName();
            }
        });
    }
}
