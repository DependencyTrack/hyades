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

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

@QuarkusTest
public class PolicyTest {

    @Test
    public void testId() {
        Policy policy = new Policy();
        policy.setId(111);
        Assertions.assertEquals(111, policy.getId());
    }

    @Test
    public void testName() {
        Policy policy = new Policy();
        policy.setName("Banned Components");
        Assertions.assertEquals("Banned Components", policy.getName());
    }

    @Test
    public void testViolationState() {
        Policy policy = new Policy();
        policy.setViolationState(Policy.ViolationState.WARN);
        Assertions.assertEquals("WARN", policy.getViolationState().name());
    }

    @Test
    public void testPolicyConditions() {
        List<PolicyCondition> conditions = new ArrayList<>();
        PolicyCondition condition = new PolicyCondition();
        conditions.add(condition);
        Policy policy = new Policy();
        policy.setPolicyConditions(conditions);
        Assertions.assertEquals(1, policy.getPolicyConditions().size());
        Assertions.assertEquals(condition, policy.getPolicyConditions().get(0));
    }

}
