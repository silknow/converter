/*
 * Copyright 2018 the original author or authors.
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

import com.google.common.collect.ImmutableList;
import org.gradle.api.NonNullApi;
import org.gradle.internal.changes.TaskStateChange;
import org.gradle.internal.changes.TaskStateChangeVisitor;

@NonNullApi
public class NoHistoryTaskUpToDateState implements TaskUpToDateState {

    public static final NoHistoryTaskUpToDateState INSTANCE = new NoHistoryTaskUpToDateState();

    private final Iterable<TaskStateChange> noHistoryTaskStateChanges;
    private final DescriptiveChange noHistoryChange = new DescriptiveChange("No history is available.");

    private NoHistoryTaskUpToDateState() {
        noHistoryTaskStateChanges = ImmutableList.<TaskStateChange>of(noHistoryChange);
    }

    @Override
    public Iterable<TaskStateChange> getInputFilesChanges() {
        throw new UnsupportedOperationException("Input file changes can only be queried when task history is available.");
    }

    @Override
    public boolean hasAnyOutputFileChanges() {
        return true;
    }

    @Override
    public void visitAllTaskChanges(TaskStateChangeVisitor visitor) {
        visitor.visitChange(noHistoryChange);
    }

    @Override
    public boolean isRebuildRequired() {
        return true;
    }
}
