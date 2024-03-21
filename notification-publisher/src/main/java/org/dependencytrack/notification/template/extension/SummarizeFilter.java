/*
 * This file is part of Dependency-Track.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) OWASP Foundation. All Rights Reserved.
 */
package org.dependencytrack.notification.template.extension;

import io.pebbletemplates.pebble.error.PebbleException;
import io.pebbletemplates.pebble.extension.Filter;
import io.pebbletemplates.pebble.template.EvaluationContext;
import io.pebbletemplates.pebble.template.PebbleTemplate;
import org.dependencytrack.proto.notification.v1.Component;
import org.dependencytrack.proto.notification.v1.Project;

import java.util.List;
import java.util.Map;

public class SummarizeFilter implements Filter {

    @Override
    public Object apply(final Object input, final Map<String, Object> args, final PebbleTemplate self,
                        final EvaluationContext context, final int lineNumber) throws PebbleException {
        if (input instanceof final Project project) {
            return summarize(project);
        } else if (input instanceof final Component component) {
            return summarize(component);
        }

        return String.valueOf(input);
    }

    @Override
    public List<String> getArgumentNames() {
        return null;
    }

    private static String summarize(final Project project) {
        if (!project.getPurl().isBlank()) {
            return project.getPurl();
        } else {
            final var sb = new StringBuilder();
            sb.append(project.getName());
            if (!project.getVersion().isBlank()) {
                sb.append(" : ").append(project.getVersion());
            }
            return sb.toString();
        }
    }

    private static String summarize(final Component component) {
        if (!component.getPurl().isBlank()) {
            return component.getPurl();
        } else {
            final var sb = new StringBuilder();
            if (!component.getGroup().isBlank()) {
                sb.append(component.getGroup()).append(" : ");
            }
            sb.append(component.getName());
            if (!component.getVersion().isBlank()) {
                sb.append(" : ").append(component.getVersion());
            }
            return sb.toString();
        }
    }

}
