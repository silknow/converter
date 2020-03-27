/*
 * Copyright 2016 the original author or authors.
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

package org.gradle.api.internal.changedetection.rules;

import com.google.common.collect.ImmutableSortedMap;
import org.gradle.api.NonNullApi;
import org.gradle.api.internal.changedetection.state.TaskExecution;
import org.gradle.internal.changes.TaskStateChangeVisitor;
import org.gradle.internal.fingerprint.FileCollectionFingerprint;

@NonNullApi
public class InputFileTaskStateChanges extends AbstractNamedFileSnapshotTaskStateChanges {
    public InputFileTaskStateChanges(TaskExecution previous, TaskExecution current) {
        super(previous, current, "Input");
    }

    @Override
    protected ImmutableSortedMap<String, ? extends FileCollectionFingerprint> getFingerprints(TaskExecution execution) {
        return execution.getInputFingerprints();
    }

    @Override
    public boolean accept(TaskStateChangeVisitor visitor) {
        return accept(visitor, true);
    }
}
