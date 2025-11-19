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

package walkingkooka.spreadsheet.terminal.storage;

import walkingkooka.spreadsheet.engine.SpreadsheetEngineContextTesting;
import walkingkooka.storage.StorageContextTesting;
import walkingkooka.terminal.TerminalContextTesting;

public interface SpreadsheetTerminalStorageContextTesting<C extends SpreadsheetTerminalStorageContext> extends TerminalContextTesting<C>,
    StorageContextTesting<C>,
    SpreadsheetEngineContextTesting<C> {

    @Override
    default String typeNamePrefix() {
        return "";
    }

    @Override
    default String typeNameSuffix() {
        return SpreadsheetTerminalStorageContext.class.getSimpleName();
    }
}
