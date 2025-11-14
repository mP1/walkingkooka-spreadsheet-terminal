/*
 * Copyright 2025 Miroslav Pokorny (github.com/mP1)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package test;


import com.google.j2cl.junit.apt.J2clTestInput;
import org.junit.Assert;
import org.junit.Test;

import walkingkooka.spreadsheet.terminal.storage.SpreadsheetTerminalStorages;

// copied from Sample
@com.google.j2cl.junit.apt.J2clTestInput(J2clTest.class)
public class J2clTest {

    @Test
    public void testAssertEquals() {
        checkEquals(
                1,
                1
        );
    }

    @Test
    public void testSpreadsheetTerminalStorages() {
        SpreadsheetTerminalStorages.metadata();
    }

    private static void checkEquals(final Object expected,
                                    final Object actual,
                                    final String message) {
        Assert.assertEquals(message, expected, actual);
    }
}


