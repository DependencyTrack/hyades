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

import io.pebbletemplates.pebble.attributes.AttributeResolver;
import io.pebbletemplates.pebble.extension.Extension;
import io.pebbletemplates.pebble.extension.Filter;
import io.pebbletemplates.pebble.extension.Function;
import io.pebbletemplates.pebble.extension.NodeVisitorFactory;
import io.pebbletemplates.pebble.extension.Test;
import io.pebbletemplates.pebble.operator.BinaryOperator;
import io.pebbletemplates.pebble.operator.UnaryOperator;
import io.pebbletemplates.pebble.tokenParser.TokenParser;

import java.util.List;
import java.util.Map;

public class CustomExtension implements Extension {

    @Override
    public Map<String, Filter> getFilters() {
        return Map.of("summarize", new SummarizeFilter());
    }

    @Override
    public Map<String, Test> getTests() {
        return null;
    }

    @Override
    public Map<String, Function> getFunctions() {
        return null;
    }

    @Override
    public List<TokenParser> getTokenParsers() {
        return null;
    }

    @Override
    public List<BinaryOperator> getBinaryOperators() {
        return null;
    }

    @Override
    public List<UnaryOperator> getUnaryOperators() {
        return null;
    }

    @Override
    public Map<String, Object> getGlobalVariables() {
        return null;
    }

    @Override
    public List<NodeVisitorFactory> getNodeVisitors() {
        return null;
    }

    @Override
    public List<AttributeResolver> getAttributeResolver() {
        return null;
    }

}
