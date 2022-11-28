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
 * Copyright (c) Steve Springett. All Rights Reserved.
 */
package org.acme.model;

import org.junit.jupiter.api.Test;
import java.util.Date;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;

public class BomTest {

    @Test
    public void testId() {
        Bom bom = new Bom();
        bom.setId(111L);
        Assertions.assertEquals(111L, bom.getId());
    }

    @Test
    public void testImported() {
        Date date = new Date();
        Bom bom = new Bom();
        bom.setImported(date);
        Assertions.assertEquals(date, bom.getImported());
    }

    @Test
    public void testProject() {
        Project project = new Project();
        Bom bom = new Bom();
        bom.setProject(project);
        Assertions.assertEquals(project, bom.getProject());
    }

    @Test
    public void testUuid() {
        UUID uuid = UUID.randomUUID();
        Bom bom = new Bom();
        bom.setUuid(uuid);
        Assertions.assertEquals(uuid.toString(), bom.getUuid().toString());
    }

    @Test
    public void testBomFormat() {
        Bom bom = new Bom();
        bom.setBomFormat(Bom.Format.CYCLONEDX);
        Assertions.assertEquals(Bom.Format.CYCLONEDX.getFormatShortName(), bom.getBomFormat());
    }

    @Test
    public void testBomSpecVersion() {
        Bom bom = new Bom();
        bom.setSpecVersion("1.1");
        Assertions.assertEquals("1.1", bom.getSpecVersion());
    }
}
