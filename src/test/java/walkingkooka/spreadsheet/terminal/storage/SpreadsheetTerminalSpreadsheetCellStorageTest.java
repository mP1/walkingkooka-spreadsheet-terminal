/*
 * Copyright 2019 Miroslav Pokorny (github.com/mP1)
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

import org.junit.jupiter.api.Test;
import walkingkooka.Either;
import walkingkooka.collect.set.Sets;
import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContextDelegator;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContexts;
import walkingkooka.spreadsheet.engine.SpreadsheetEngines;
import walkingkooka.spreadsheet.engine.collection.SpreadsheetCellSet;
import walkingkooka.spreadsheet.expression.SpreadsheetExpressionEvaluationContext;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterSelector;
import walkingkooka.spreadsheet.formula.SpreadsheetFormula;
import walkingkooka.spreadsheet.formula.parser.SpreadsheetFormulaParserToken;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStores;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReferenceLoader;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.security.store.SpreadsheetGroupStores;
import walkingkooka.spreadsheet.security.store.SpreadsheetUserStores;
import walkingkooka.spreadsheet.store.SpreadsheetCellRangeStores;
import walkingkooka.spreadsheet.store.SpreadsheetCellReferencesStores;
import walkingkooka.spreadsheet.store.SpreadsheetCellStores;
import walkingkooka.spreadsheet.store.SpreadsheetColumnStores;
import walkingkooka.spreadsheet.store.SpreadsheetLabelReferencesStores;
import walkingkooka.spreadsheet.store.SpreadsheetLabelStores;
import walkingkooka.spreadsheet.store.SpreadsheetRowStores;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepositories;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.spreadsheet.validation.form.store.SpreadsheetFormStores;
import walkingkooka.storage.StoragePath;
import walkingkooka.storage.StorageTesting;
import walkingkooka.storage.StorageValue;
import walkingkooka.storage.StorageValueInfo;
import walkingkooka.storage.Storages;
import walkingkooka.text.cursor.TextCursor;
import walkingkooka.tree.text.TextNode;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetTerminalSpreadsheetCellStorageTest implements StorageTesting<SpreadsheetTerminalSpreadsheetCellStorage, SpreadsheetTerminalStorageContext>,
    SpreadsheetMetadataTesting {

    @Test
    public void testWithNullSpreadsheetEngineFails() {
        assertThrows(
            NullPointerException.class,
            () -> SpreadsheetTerminalSpreadsheetCellStorage.with(null)
        );
    }

    @Test
    public void testLoadMissingCell() {
        final TestSpreadsheetTerminalStorageContext context = new TestSpreadsheetTerminalStorageContext();

        final StoragePath path = StoragePath.parse("/A1");

        this.loadAndCheck(
            this.createStorage(),
            path,
            context,
            StorageValue.with(
                path,
                Optional.empty()
            )
        );
    }

    @Test
    public void testLoadCell() {
        final TestSpreadsheetTerminalStorageContext context = new TestSpreadsheetTerminalStorageContext();

        final SpreadsheetCell cell = SpreadsheetSelection.A1.setFormula(
            SpreadsheetFormula.EMPTY.setText("=1")
        );

        SpreadsheetEngines.basic()
            .saveCell(
                cell,
                context
            );

        final StoragePath path = StoragePath.parse("/A1");

        this.loadAndCheck(
            this.createStorage(),
            path,
            context,
            StorageValue.with(
                path,
                Optional.of(
                    SpreadsheetCellSet.EMPTY.concat(
                        context.storeRepository()
                            .cells()
                            .loadOrFail(cell.reference())
                    )
                )
            )
        );
    }

    @Test
    public void testLoadCellRange() {
        final TestSpreadsheetTerminalStorageContext context = new TestSpreadsheetTerminalStorageContext();

        final SpreadsheetCell a1 = SpreadsheetSelection.A1.setFormula(
            SpreadsheetFormula.EMPTY.setText("=1")
        );

        final SpreadsheetCell a2 = SpreadsheetSelection.parseCell("A2")
            .setFormula(
                SpreadsheetFormula.EMPTY.setText("=2")
            );

        SpreadsheetEngines.basic()
            .saveCells(
                Sets.of(
                    a1,
                    a2
                ),
                context
            );

        final StoragePath path = StoragePath.parse("/A1:A2");

        this.loadAndCheck(
            this.createStorage(),
            path,
            context,
            StorageValue.with(
                path,
                Optional.of(
                    SpreadsheetCellSet.EMPTY.concat(
                        context.storeRepository()
                            .cells()
                            .loadOrFail(a1.reference())
                    ).concat(
                        context.storeRepository()
                            .cells()
                            .loadOrFail(a2.reference())
                    )
                )
            )
        );
    }

    @Test
    public void testSave() {
        final TestSpreadsheetTerminalStorageContext context = new TestSpreadsheetTerminalStorageContext();


        final SpreadsheetCell cell = SpreadsheetEngines.basic()
            .saveCell(
                SpreadsheetSelection.A1.setFormula(
                    SpreadsheetFormula.EMPTY.setText("=1")
                ),
                context
            ).cells()
            .iterator()
            .next();

        final StoragePath path = StoragePath.parse("/A1");

        this.saveAndCheck(
            this.createStorage(),
            StorageValue.with(
                path,
                Optional.of(cell)
            ),
            context,
            StorageValue.with(
                path,
                Optional.of(
                    SpreadsheetCellSet.EMPTY.concat(
                        context.storeRepository()
                            .cells()
                            .loadOrFail(cell.reference())
                    )
                )
            )
        );
    }

    @Test
    public void testDelete() {
        final TestSpreadsheetTerminalStorageContext context = new TestSpreadsheetTerminalStorageContext();

        final SpreadsheetCell cell = SpreadsheetSelection.A1.setFormula(
            SpreadsheetFormula.EMPTY.setText("=1")
        );

        SpreadsheetEngines.basic()
            .saveCell(
                cell,
                context
            );

        final StoragePath path = StoragePath.parse("/A1");

        final SpreadsheetTerminalSpreadsheetCellStorage storage = this.createStorage();
        storage.delete(
            path,
            context
        );

        this.loadAndCheck(
            storage,
            path,
            context,
            StorageValue.with(
                path,
                Optional.empty()
            )
        );
    }

    @Test
    public void testList() {
        final TestSpreadsheetTerminalStorageContext context = new TestSpreadsheetTerminalStorageContext();

        final SpreadsheetCell a1 = SpreadsheetSelection.A1.setFormula(
            SpreadsheetFormula.EMPTY.setText("=1")
        );

        final SpreadsheetCell a2 = SpreadsheetSelection.parseCell("A2")
            .setFormula(
                SpreadsheetFormula.EMPTY.setText("=2")
            );

        final SpreadsheetCell a3 = SpreadsheetSelection.parseCell("A3")
            .setFormula(
                SpreadsheetFormula.EMPTY.setText("=3")
            );

        SpreadsheetEngines.basic()
            .saveCells(
                Sets.of(
                    a1,
                    a2,
                    a3
                ),
                context
            );

        final StoragePath path = StoragePath.parse("/A1:C2");

        this.listAndCheck(
            this.createStorage(),
            path,
            0,
            2,
            context,
            StorageValueInfo.with(
                StoragePath.parse("/A1"),
                context.createdAuditInfo()
            ),
            StorageValueInfo.with(
                StoragePath.parse("/A2"),
                context.createdAuditInfo()
            )
        );
    }

    @Override
    public SpreadsheetTerminalSpreadsheetCellStorage createStorage() {
        return SpreadsheetTerminalSpreadsheetCellStorage.with(SpreadsheetEngines.basic());
    }

    @Override
    public SpreadsheetTerminalStorageContext createContext() {
        return SpreadsheetTerminalStorageContexts.fake();
    }

    static class TestSpreadsheetTerminalStorageContext extends FakeSpreadsheetTerminalStorageContext implements SpreadsheetEngineContextDelegator {

        @Override
        public SpreadsheetStoreRepository storeRepository() {
            return this.engineContext.storeRepository();
        }

        @Override
        public SpreadsheetExpressionEvaluationContext spreadsheetExpressionEvaluationContext(final Optional<SpreadsheetCell> cell,
                                                                                             final SpreadsheetExpressionReferenceLoader loader) {
            return this.engineContext.spreadsheetExpressionEvaluationContext(cell, loader);
        }

        @Override
        public SpreadsheetCell formatValueAndStyle(SpreadsheetCell cell, Optional<SpreadsheetFormatterSelector> formatter) {
            return this.engineContext.formatValueAndStyle(cell, formatter);
        }

        @Override
        public Optional<TextNode> formatValue(SpreadsheetCell cell, Optional<Object> value, Optional<SpreadsheetFormatterSelector> formatter) {
            return this.engineContext.formatValue(cell, value, formatter);
        }

        @Override
        public SpreadsheetFormulaParserToken parseFormula(TextCursor formula, Optional<SpreadsheetCell> cell) {
            return this.engineContext.parseFormula(formula, cell);
        }

        @Override
        public SpreadsheetMetadata spreadsheetMetadata() {
            return this.engineContext.spreadsheetMetadata();
        }

        @Override
        public <T> Either<T, String> convert(Object o, Class<T> aClass) {
            return this.engineContext.convert(o, aClass);
        }

        @Override
        public boolean canConvert(Object o, Class<?> aClass) {
            return this.engineContext.canConvert(o, aClass);
        }

        @Override
        public LocalDateTime now() {
            return this.engineContext.now();
        }

        @Override
        public Optional<EmailAddress> user() {
            return this.engineContext.user();
        }

        @Override
        public SpreadsheetEngineContext spreadsheetEngineContext() {
            return this.engineContext;
        }

        private final SpreadsheetEngineContext engineContext = BasicSpreadsheetTerminalStorageContext.with(
            SpreadsheetEngineContexts.basic(
                AbsoluteUrl.parseAbsolute("https://example.com"),
                METADATA_EN_AU.set(
                    SpreadsheetMetadataPropertyName.LOCALE,
                    LOCALE
                ),
                SpreadsheetStoreRepositories.basic(
                    SpreadsheetCellStores.treeMap(),
                    SpreadsheetCellReferencesStores.treeMap(),
                    SpreadsheetColumnStores.treeMap(),
                    SpreadsheetFormStores.treeMap(),
                    SpreadsheetGroupStores.treeMap(),
                    SpreadsheetLabelStores.treeMap(),
                    SpreadsheetLabelReferencesStores.treeMap(),
                    SpreadsheetMetadataStores.treeMap(
                        METADATA_EN_AU,
                        NOW
                    ),
                    SpreadsheetCellRangeStores.treeMap(),
                    SpreadsheetCellRangeStores.treeMap(),
                    SpreadsheetRowStores.treeMap(),
                    Storages.tree(),
                    SpreadsheetUserStores.treeMap()
                ),
                SpreadsheetMetadataPropertyName.SCRIPTING_FUNCTIONS,
                SpreadsheetMetadataTesting.ENVIRONMENT_CONTEXT,
                SpreadsheetMetadataTesting.LOCALE_CONTEXT,
                SpreadsheetMetadataTesting.TERMINAL_CONTEXT,
                SpreadsheetMetadataTesting.SPREADSHEET_PROVIDER,
                SpreadsheetMetadataTesting.PROVIDER_CONTEXT
            ),
            TERMINAL_CONTEXT
        );
    }

    // class............................................................................................................

    @Override
    public Class<SpreadsheetTerminalSpreadsheetCellStorage> type() {
        return SpreadsheetTerminalSpreadsheetCellStorage.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }
}
