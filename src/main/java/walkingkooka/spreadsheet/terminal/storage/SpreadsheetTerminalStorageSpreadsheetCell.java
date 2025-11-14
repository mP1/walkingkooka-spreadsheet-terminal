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
import walkingkooka.collect.set.Sets;
import walkingkooka.net.header.MediaType;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetMediaTypes;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetDeltaProperties;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineEvaluation;
import walkingkooka.spreadsheet.engine.collection.SpreadsheetCellSet;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReference;
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
 * A {@link Storage} that maps cells to a {@link Storage}, for the current spreadsheet.
 * <pre>
 * /cell/SpreadsheetExpressionReference
 * /cell/SpreadsheetExpressionReference/compute-if-necessary
 * </pre>
 * for the {@link StorageValue}.
 */
final class SpreadsheetTerminalStorageSpreadsheetCell extends SpreadsheetTerminalStorage {

    static SpreadsheetTerminalStorageSpreadsheetCell with(final SpreadsheetEngine engine) {
        return new SpreadsheetTerminalStorageSpreadsheetCell(engine);
    }

    private final static MediaType MEDIA_TYPE = SpreadsheetMediaTypes.MEMORY_CELL;

    private SpreadsheetTerminalStorageSpreadsheetCell(final SpreadsheetEngine engine) {
        super();

        this.engine = Objects.requireNonNull(engine, "engine");
    }

    @Override
    Optional<StorageValue> loadNonNull(final StoragePath path,
                                       final SpreadsheetTerminalStorageContext context) {
        StorageValue value = null;

        final List<StorageName> names = path.namesList();

        SpreadsheetExpressionReference cellOrLabels = null;
        SpreadsheetEngineEvaluation evaluation = SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY;

        // SLASH A1 compute-if-necessary
        switch (names.size()) {
            case 2:
            case 3:
                cellOrLabels = context.convertOrFail(
                    names.get(1)
                        .value(),
                    SpreadsheetExpressionReference.class
                );
                break;
            default:
                break;
        }

        if (3 == names.size()) {
            evaluation = SpreadsheetEngineEvaluation.parse(
                names.get(2)
                    .value()
            );
        }

        if (null != cellOrLabels) {
            final SpreadsheetDelta delta = this.engine.loadCells(
                cellOrLabels,
                evaluation,
                CELLS_ONLY,
                context
            );

            final Set<SpreadsheetCell> cells = delta.cells();
            if (false == cells.isEmpty()) {
                value = StorageValue.with(
                    path,
                    Optional.of(cells)
                ).setContentType(MEDIA_TYPE);
            }
        }

        return Optional.ofNullable(value);
    }

    /**
     * Select only cells to appear in the response.
     */
    private final static Set<SpreadsheetDeltaProperties> CELLS_ONLY = Sets.of(SpreadsheetDeltaProperties.CELLS);

    @Override
    StorageValue saveNonNull(final StorageValue value,
                             final SpreadsheetTerminalStorageContext context) {
        switch (value.path()
            .namesList()
            .size()) {
            case 0:
            case 1:
                break;
            default:
                throw new IllegalArgumentException("Invalid path, must not contain selection");
        }

        final SpreadsheetCellSet cells = context.convertOrFail(
            value.value()
                .orElse(null),
            SpreadsheetCellSet.class
        );

        return value.setValue(
            Optional.of(
                this.engine.saveCells(
                    cells,
                    context
                ).cells()
            )
        ).setContentType(MEDIA_TYPE);
    }

    /**
     * Deletes the given cells. Note if the path contains additional components a {@link IllegalArgumentException}
     * will be thrown.
     */
    @Override
    void deleteNonNull(final StoragePath path,
                       final SpreadsheetTerminalStorageContext context) {
        final List<StorageName> names = path.namesList();
        switch (names.size()) {
            case 0:
            case 1:
                throw new IllegalArgumentException("Missing selection");
            case 2:
                this.engine.deleteCells(
                    context.convertOrFail(
                        names.get(1)
                            .value(),
                        SpreadsheetExpressionReference.class
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

        final SpreadsheetExpressionReference cellOrLabels;

        switch (names.size()) {
            case 2:
                cellOrLabels = context.convertOrFail(
                    names.get(1)
                        .value(),
                    SpreadsheetExpressionReference.class
                );
                break;
            default:
                throw new IllegalArgumentException("Invalid path");
        }

        final SpreadsheetDelta delta = this.engine.loadCells(
            cellOrLabels,
            SpreadsheetEngineEvaluation.SKIP_EVALUATE,
            CELLS_ONLY,
            context
        );

        return delta.cells()
            .stream()
            .map(
                (SpreadsheetCell c) -> StorageValueInfo.with(
                    StoragePath.ROOT.append(
                        StorageName.with(c.reference().text())
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
