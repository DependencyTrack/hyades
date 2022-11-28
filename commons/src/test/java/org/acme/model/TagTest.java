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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class TagTest { 

    @Test
    public void testId() { 
        Tag tag = new Tag();
        tag.setId(111);
        Assertions.assertEquals(111L, tag.getId());
    } 

    @Test
    public void testName() {
        Tag tag = new Tag();
        tag.setName("java");
        Assertions.assertEquals("java", tag.getName());
    } 

    @Test
    public void testProjects() {
        List<Project> projects = new ArrayList<>();
        Project project = new Project();
        projects.add(project);
        Tag tag = new Tag();
        tag.setProjects(projects);
        Assertions.assertEquals(1, tag.getProjects().size());
        Assertions.assertEquals(project, tag.getProjects().get(0));
    } 

    @Test
    public void testEquals() {
        Tag t1 = new Tag();
        t1.setId(111);
        Tag t2 = new Tag();
        t2.setId(222);
        Tag t3 = new Tag();
        t3.setId(111);
        Assertions.assertFalse(t1.equals(t2));
        Assertions.assertTrue(t1.equals(t3));
    }
} 
