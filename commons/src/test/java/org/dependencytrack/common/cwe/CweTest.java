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
package org.dependencytrack.common.cwe;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CweTest {

    @Test
    public void testId() {
        Cwe cwe = new Cwe();
        cwe.setId(111);
        Assertions.assertEquals(111, cwe.getId());
    }

    @Test
    public void testCweId() {
        Cwe cwe = new Cwe();
        cwe.setCweId(79);
        Assertions.assertEquals(79, cwe.getCweId());
    }

    @Test
    public void testName() {
        Cwe cwe = new Cwe();
        cwe.setName("Improper Neutralization of Input During Web Page Generation ('Cross-site Scripting')");
        Assertions.assertEquals("Improper Neutralization of Input During Web Page Generation ('Cross-site Scripting')", cwe.getName());
    }
} 
