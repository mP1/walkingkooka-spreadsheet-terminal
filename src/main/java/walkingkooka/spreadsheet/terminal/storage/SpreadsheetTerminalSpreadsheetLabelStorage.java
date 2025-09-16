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

import walkingkooka.collect.list.ImmutableList;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.store.SpreadsheetCellStore;
import walkingkooka.storage.Storage;
import walkingkooka.storage.StorageName;
import walkingkooka.storage.StoragePath;
import walkingkooka.storage.StorageValue;
import walkingkooka.storage.StorageValueInfo;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * A {@link Storage} that maps {@link SpreadsheetLabelMapping} to a {@link Storage}, for the current spreadsheet.
 * <pre>
 * /label/SpreadsheetLabelName
 * </pre>
 * for the {@link StorageValue}.
 */
final class SpreadsheetTerminalSpreadsheetLabelStorage extends SpreadsheetTerminalStorage {

    static SpreadsheetTerminalSpreadsheetLabelStorage with(final SpreadsheetEngine engine) {
        return new SpreadsheetTerminalSpreadsheetLabelStorage(engine);
    }

    private SpreadsheetTerminalSpreadsheetLabelStorage(final SpreadsheetEngine engine) {
        super(engine);
    }

    @Override
    Optional<StorageValue> loadNonNull(final StoragePath path,
                                       final SpreadsheetTerminalStorageContext context) {
        StorageValue value = null;

        final List<StorageName> names = path.namesList();

        SpreadsheetLabelName labelName = null;

        switch (names.size()) {
            case 2:
                labelName = context.convertOrFail(
                    names.get(1)
                        .value(),
                    SpreadsheetLabelName.class
                );
                break;
            default:
                break;
        }

        if (null != labelName) {
            final SpreadsheetDelta delta = this.engine.loadLabel(
                labelName,
                context
            );

            final Set<SpreadsheetLabelMapping> mappings = delta.labels();
            value = StorageValue.with(
                path,
                Optional.ofNullable(
                    mappings.isEmpty() ?
                        null :
                        mappings
                )
            );
        }

        return Optional.ofNullable(value);
    }


    @Override
    StorageValue saveNonNull(final StorageValue value,
                             final SpreadsheetTerminalStorageContext context) {
        SpreadsheetLabelMapping labelMapping = context.convertOrFail(
            value.value()
                .orElse(null),
            SpreadsheetLabelMapping.class
        );

        return value.setValue(
            Optional.of(
                this.engine.saveLabel(
                    labelMapping,
                    context
                ).labels()
            )
        );
    }

    @Override
    void deleteNonNull(final StoragePath path,
                       final SpreadsheetTerminalStorageContext context) {
        final List<StorageName> names = path.namesList();
        switch (names.size()) {
            case 0:
            case 1:
                throw new IllegalArgumentException("Missing selection");
            case 2:
                this.engine.deleteLabel(
                    context.convertOrFail(
                        names.get(1)
                            .value(),
                        SpreadsheetLabelName.class
                    ),
                    context
                );
                break;
            default:
                throw new IllegalArgumentException("Invalid path");
        }
    }

    @Override
    List<StorageValueInfo> listNonNull(final StoragePath path,
                                       final int offset,
                                       final int count,
                                       final SpreadsheetTerminalStorageContext context) {
        final List<StorageName> names = path.namesList();

        final SpreadsheetLabelName labelName;

        switch (names.size()) {
            case 2:
                labelName = context.convertOrFail(
                    names.get(1)
                        .value(),
                    SpreadsheetLabelName.class
                );
                break;
            default:
                throw new IllegalArgumentException("Invalid path");
        }

        final SpreadsheetDelta delta = this.engine.loadLabel(
            labelName,
            context
        );

        return delta.labels()
            .stream()
            .map(
                (SpreadsheetLabelMapping m) -> StorageValueInfo.with(
                    StoragePath.ROOT.append(
                        StorageName.with(
                            m.label()
                                .text()
                        )
                    ),
                    context.createdAuditInfo()
                )
            ).collect(ImmutableList.collector());
    }

    // Object...........................................................................................................

    @Override
    public String toString() {
        return SpreadsheetCellStore.class.getSimpleName();
    }
}
