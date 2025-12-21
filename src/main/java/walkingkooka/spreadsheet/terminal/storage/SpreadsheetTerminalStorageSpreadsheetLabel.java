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
import walkingkooka.net.header.MediaType;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.net.SpreadsheetMediaTypes;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.store.SpreadsheetCellStore;
import walkingkooka.storage.Storage;
import walkingkooka.storage.StorageName;
import walkingkooka.storage.StoragePath;
import walkingkooka.storage.StorageValue;
import walkingkooka.storage.StorageValueInfo;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * A {@link Storage} that maps {@link SpreadsheetLabelMapping} to a {@link Storage}, for the current spreadsheet.
 * <pre>
 * /label/SpreadsheetLabelName
 * </pre>
 * for the {@link StorageValue}.
 */
final class SpreadsheetTerminalStorageSpreadsheetLabel extends SpreadsheetTerminalStorage {

    static SpreadsheetTerminalStorageSpreadsheetLabel with(final SpreadsheetEngine engine) {
        return new SpreadsheetTerminalStorageSpreadsheetLabel(engine);
    }

    private final static MediaType MEDIA_TYPE = SpreadsheetMediaTypes.MEMORY_LABEL;

    private SpreadsheetTerminalStorageSpreadsheetLabel(final SpreadsheetEngine engine) {
        super();

        this.engine = Objects.requireNonNull(engine, "engine");
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
                throw new IllegalArgumentException("Invalid path after label name");
        }

        if (null != labelName) {
            final SpreadsheetDelta delta = this.engine.loadLabel(
                labelName,
                context
            );

            final Set<SpreadsheetLabelMapping> mappings = delta.labels();

            if (false == mappings.isEmpty()) {
                value = StorageValue.with(
                    path,
                    Optional.ofNullable(
                        mappings.isEmpty() ?
                            null :
                            mappings.iterator()
                                .next()
                    )
                ).setContentType(MEDIA_TYPE);
            }
        }

        return Optional.ofNullable(value);
    }

    @Override
    StorageValue saveNonNull(final StorageValue value,
                             final SpreadsheetTerminalStorageContext context) {
        final List<StorageName> names = value.path()
            .namesList();
        switch (names.size()) {
            case 0:
            case 1:
                throw new IllegalArgumentException("Missing label");
            case 2:
                SpreadsheetLabelMapping labelMapping = context.convertOrFail(
                    value.value()
                        .orElseThrow(() -> new IllegalArgumentException("Missing " + SpreadsheetLabelMapping.class.getSimpleName())),
                    SpreadsheetLabelMapping.class
                );

                final Set<SpreadsheetLabelMapping> saved = this.engine.saveLabel(
                    labelMapping,
                    context
                ).labels();

                return value.setValue(
                    Optional.ofNullable(
                        saved.isEmpty() ?
                            null :
                            saved.iterator()
                                .next()
                    )
                ).setContentType(MEDIA_TYPE);
            default:
                throw new IllegalArgumentException("Invalid path after label");
        }
    }

    @Override
    void deleteNonNull(final StoragePath path,
                       final SpreadsheetTerminalStorageContext context) {
        final List<StorageName> names = path.namesList();
        switch (names.size()) {
            case 0:
            case 1:
                throw new IllegalArgumentException("Missing label");
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
                throw new IllegalArgumentException("Invalid path after label");
        }
    }

    @Override
    List<StorageValueInfo> listNonNull(final StoragePath path,
                                       final int offset,
                                       final int count,
                                       final SpreadsheetTerminalStorageContext context) {
        final List<StorageName> names = path.namesList();

        final String labelName;

        switch (names.size()) {
            case 0:
            case 1:
                labelName = "";
                break;
            case 2:
                labelName = names.get(1)
                    .value();
                break;
            default:
                throw new IllegalArgumentException("Invalid path after label");
        }

        final SpreadsheetDelta delta = this.engine.findLabelsByName(
            labelName,
            offset,
            count,
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

    private final SpreadsheetEngine engine;

    // Object...........................................................................................................

    @Override
    public String toString() {
        return SpreadsheetCellStore.class.getSimpleName();
    }
}
