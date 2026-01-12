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
     * {@see SpreadsheetTerminalStorageSpreadsheetCell}
     */
    public static Storage<SpreadsheetTerminalStorageContext> cell(final SpreadsheetEngine engine) {
        return SpreadsheetTerminalStorageSpreadsheetCell.with(engine);
    }

    /**
     * {@see SpreadsheetTerminalStorageSpreadsheetLabel}
     */
    public static Storage<SpreadsheetTerminalStorageContext> label(final SpreadsheetEngine engine) {
        return SpreadsheetTerminalStorageSpreadsheetLabel.with(engine);
    }

    /**
     * {@see SpreadsheetTerminalStorageSpreadsheetMetadata}
     */
    public static Storage<SpreadsheetTerminalStorageContext> metadata() {
        return SpreadsheetTerminalStorageSpreadsheetMetadata.INSTANCE;
    }

    /**
     * {@see SpreadsheetTerminalStorageRouter}
     */
    public static Storage<SpreadsheetTerminalStorageContext> router(final Storage<SpreadsheetTerminalStorageContext> cells,
                                                                    final Storage<SpreadsheetTerminalStorageContext> labels,
                                                                    final Storage<SpreadsheetTerminalStorageContext> metadatas,
                                                                    final Storage<SpreadsheetTerminalStorageContext> other) {
        return SpreadsheetTerminalStorageRouter.with(
            cells,
            labels,
            metadatas,
            other
        );
    }

    /**
     * Stop creation
     */
    private SpreadsheetTerminalStorages() {
        throw new UnsupportedOperationException();
    }
}
