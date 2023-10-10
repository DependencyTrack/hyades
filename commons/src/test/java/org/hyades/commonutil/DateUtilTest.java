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
package org.hyades.commonutil;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;

public class DateUtilTest {

    @Test
    public void testParseDate() {
        Date date = DateUtil.parseDate("20191231153012", "yyyyMMddHHmmss");
        LocalDateTime localDateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        Assertions.assertEquals(Month.DECEMBER, localDateTime.getMonth());
        Assertions.assertEquals(31, localDateTime.getDayOfMonth());
        Assertions.assertEquals(2019, localDateTime.getYear());
        Assertions.assertEquals(15, localDateTime.getHour());
        Assertions.assertEquals(30, localDateTime.getMinute());
        Assertions.assertEquals(12, localDateTime.getSecond());
    }

    @Test
    public void testParseGMTDate() {
        Date date = DateUtil.parseDate("Thu, 07 Jul 2022 14:00:00 GMT", "EEE, dd MMM yyyy HH:mm:ss Z");
        LocalDateTime localDateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        Assertions.assertEquals(Month.JULY, localDateTime.getMonth());
        Assertions.assertEquals(7, localDateTime.getDayOfMonth());
        Assertions.assertEquals(2022, localDateTime.getYear());
        Assertions.assertNotNull(localDateTime.getHour());
        Assertions.assertEquals(0, localDateTime.getMinute());
        Assertions.assertEquals(0, localDateTime.getSecond());
    }

    @Test
    public void testToISO8601() {
        Date date = Date.from(LocalDateTime.of(2019, Month.JANUARY, 31, 15, 30, 12).toInstant(ZoneOffset.UTC));
        String iso8601Date = DateUtil.toISO8601(date);
        Assertions.assertEquals("2019-01-31T15:30:12Z", iso8601Date);
    }
}
