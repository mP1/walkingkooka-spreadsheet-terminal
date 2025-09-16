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

import walkingkooka.reflect.PublicStaticHelper;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.storage.Storage;

/**
 * A collection of {@link walkingkooka.storage.Storage} for a spreadsheet terminal.
 */
public final class SpreadsheetTerminalStorages implements PublicStaticHelper {

    /**
     * {@see SpreadsheetTerminalSpreadsheetCellStorage}
     */
    public static Storage<SpreadsheetTerminalStorageContext> cell(final SpreadsheetEngine engine) {
        return SpreadsheetTerminalSpreadsheetCellStorage.with(engine);
    }

    /**
     * {@see SpreadsheetTerminalSpreadsheetLabelStorage}
     */
    public static Storage<SpreadsheetTerminalStorageContext> label(final SpreadsheetEngine engine) {
        return SpreadsheetTerminalSpreadsheetLabelStorage.with(engine);
    }

    /**
     * {@see SpreadsheetTerminalSpreadsheetMetadataStorage}
     */
    public static Storage<SpreadsheetTerminalStorageContext> metadata() {
        return SpreadsheetTerminalSpreadsheetMetadataStorage.INSTANCE;
    }

    /**
     * Stop creation
     */
    private SpreadsheetTerminalStorages() {
        throw new UnsupportedOperationException();
    }
}
