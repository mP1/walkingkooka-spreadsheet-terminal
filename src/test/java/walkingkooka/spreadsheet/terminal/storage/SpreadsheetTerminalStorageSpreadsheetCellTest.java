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

import org.junit.jupiter.api.Test;
import walkingkooka.Either;
import walkingkooka.collect.set.Sets;
import walkingkooka.convert.ConverterException;
import walkingkooka.environment.EnvironmentContexts;
import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.route.Router;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetContexts;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.SpreadsheetMediaTypes;
import walkingkooka.spreadsheet.compare.provider.SpreadsheetComparatorAliasSet;
import walkingkooka.spreadsheet.convert.provider.SpreadsheetConvertersConverterProviders;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContextDelegator;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContexts;
import walkingkooka.spreadsheet.engine.SpreadsheetEngines;
import walkingkooka.spreadsheet.engine.SpreadsheetMetadataMode;
import walkingkooka.spreadsheet.engine.collection.SpreadsheetCellSet;
import walkingkooka.spreadsheet.export.provider.SpreadsheetExporterAliasSet;
import walkingkooka.spreadsheet.expression.SpreadsheetExpressionEvaluationContext;
import walkingkooka.spreadsheet.expression.SpreadsheetExpressionFunctions;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterAliasSet;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterSelector;
import walkingkooka.spreadsheet.formula.SpreadsheetFormula;
import walkingkooka.spreadsheet.formula.parser.SpreadsheetFormulaParserToken;
import walkingkooka.spreadsheet.importer.provider.SpreadsheetImporterAliasSet;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStore;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStores;
import walkingkooka.spreadsheet.parser.SpreadsheetParser;
import walkingkooka.spreadsheet.parser.provider.SpreadsheetParserAliasSet;
import walkingkooka.spreadsheet.parser.provider.SpreadsheetParserSelector;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReferenceLoader;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepositories;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.storage.StoragePath;
import walkingkooka.storage.StorageTesting;
import walkingkooka.storage.StorageValue;
import walkingkooka.storage.StorageValueInfo;
import walkingkooka.text.cursor.TextCursor;
import walkingkooka.tree.expression.Expression;
import walkingkooka.tree.text.TextNode;
import walkingkooka.validation.form.provider.FormHandlerAliasSet;
import walkingkooka.validation.provider.ValidatorAliasSet;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetTerminalStorageSpreadsheetCellTest implements StorageTesting<SpreadsheetTerminalStorageSpreadsheetCell, SpreadsheetTerminalStorageContext>,
    SpreadsheetMetadataTesting {

    @Test
    public void testWithNullSpreadsheetEngineFails() {
        assertThrows(
            NullPointerException.class,
            () -> SpreadsheetTerminalStorageSpreadsheetCell.with(null)
        );
    }

    @Test
    public void testLoadMissingCellReference() {
        final TestSpreadsheetTerminalStorageContext context = new TestSpreadsheetTerminalStorageContext();

        final StoragePath path = StoragePath.ROOT;

        this.loadAndCheck(
            this.createStorage(),
            path,
            context
        );
    }

    @Test
    public void testLoadInvalidCellReferenceFails() {
        final TestSpreadsheetTerminalStorageContext context = new TestSpreadsheetTerminalStorageContext();

        final StoragePath path = StoragePath.parse("/999");

        assertThrows(
            ConverterException.class,
            () -> this.createStorage()
                .load(
                    path,
                    context
                )
        );
    }

    @Test
    public void testLoadMissingCell() {
        final TestSpreadsheetTerminalStorageContext context = new TestSpreadsheetTerminalStorageContext();

        final StoragePath path = StoragePath.parse("/A1");

        this.loadAndCheck(
            this.createStorage(),
            path,
            context
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
            ).setContentType(SpreadsheetMediaTypes.MEMORY_CELL)
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
            ).setContentType(SpreadsheetMediaTypes.MEMORY_CELL)
        );
    }

    @Test
    public void testSaveWithInvalidCellReferenceFails() {
        final IllegalArgumentException thrown = assertThrows(
            IllegalArgumentException.class,
            () -> this.createStorage()
                .save(
                    StorageValue.with(
                        StoragePath.parse("/999"),
                        Optional.of(
                            SpreadsheetSelection.A1.setFormula(SpreadsheetFormula.EMPTY)
                        )
                    ),
                    new TestSpreadsheetTerminalStorageContext()
                )
        );

        this.checkEquals(
            "Invalid path, must not contain selection",
            thrown.getMessage()
        );
    }

    @Test
    public void testSaveWithStorageValueMissingCell() {
        final TestSpreadsheetTerminalStorageContext context = new TestSpreadsheetTerminalStorageContext();

        final StoragePath path = StoragePath.ROOT;

        this.saveAndCheck(
            this.createStorage(),
            StorageValue.with(
                path,
                Optional.empty()
            ),
            context,
            StorageValue.with(
                path,
                Optional.empty()
            ).setContentType(SpreadsheetMediaTypes.MEMORY_CELL)
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

        final StoragePath path = StoragePath.ROOT;

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
            ).setContentType(SpreadsheetMediaTypes.MEMORY_CELL)
        );
    }

    @Test
    public void testDeleteMissingReferenceFails() {
        final IllegalArgumentException thrown = assertThrows(
            IllegalArgumentException.class,
            () -> this.createStorage()
                .delete(
                    StoragePath.ROOT,
                    new TestSpreadsheetTerminalStorageContext()
                )
        );

        this.checkEquals(
            "Missing selection",
            thrown.getMessage()
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

        final SpreadsheetTerminalStorageSpreadsheetCell storage = this.createStorage();
        storage.delete(
            path,
            context
        );

        this.loadAndCheck(
            storage,
            path,
            context
        );
    }

    @Test
    public void testListWithExtraPathFails() {
        final IllegalArgumentException thrown = assertThrows(
            IllegalArgumentException.class,
            () -> this.createStorage()
                .list(
                    StoragePath.parse("/A1/extra"),
                    0,
                    1,
                    new TestSpreadsheetTerminalStorageContext()
                )
        );

        this.checkEquals(
            "Invalid path after selection",
            thrown.getMessage()
        );
    }

    @Test
    public void testListWithoutSelection() {
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

        this.listAndCheck(
            this.createStorage(),
            StoragePath.ROOT,
            0,
            3,
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
    public SpreadsheetTerminalStorageSpreadsheetCell createStorage() {
        return SpreadsheetTerminalStorageSpreadsheetCell.with(SpreadsheetEngines.basic());
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
            return this.engineContext.spreadsheetExpressionEvaluationContext(
                cell,
                loader
            );
        }

        @Override
        public SpreadsheetCell formatValueAndStyle(final SpreadsheetCell cell,
                                                   final Optional<SpreadsheetFormatterSelector> formatter) {
            return this.engineContext.formatValueAndStyle(
                cell,
                formatter
            );
        }

        @Override
        public Optional<TextNode> formatValue(final SpreadsheetCell cell,
                                              final Optional<Object> value,
                                              final Optional<SpreadsheetFormatterSelector> formatter) {
            return this.engineContext.formatValue(
                cell,
                value,
                formatter
            );
        }

        @Override
        public SpreadsheetFormulaParserToken parseFormula(final TextCursor formula,
                                                          final Optional<SpreadsheetCell> cell) {
            return this.engineContext.parseFormula(
                formula,
                cell
            );
        }

        @Override
        public Optional<Expression> toExpression(final SpreadsheetFormulaParserToken token) {
            Objects.requireNonNull(token, "token");
            return this.engineContext.toExpression(token);
        }

        @Override
        public SpreadsheetMetadata spreadsheetMetadata() {
            return this.engineContext.spreadsheetMetadata();
        }

        @Override
        public <T> Either<T, String> convert(final Object value,
                                             final Class<T> type) {
            return this.engineContext.convert(
                value,
                type
            );
        }

        @Override
        public boolean canConvert(final Object value,
                                  final Class<?> type) {
            return this.engineContext.canConvert(
                value,
                type
            );
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

        private final SpreadsheetEngineContext engineContext = createSpreadsheetEngineContext();

        private SpreadsheetEngineContext createSpreadsheetEngineContext() {
            final SpreadsheetId id = SpreadsheetId.with(1);
            final SpreadsheetMetadata metadata = METADATA_EN_AU.set(
                SpreadsheetMetadataPropertyName.LOCALE,
                SpreadsheetTerminalStorageSpreadsheetCellTest.LOCALE
            ).set(
                SpreadsheetMetadataPropertyName.SPREADSHEET_ID,
                id
            ).set(
                SpreadsheetMetadataPropertyName.COMPARATORS,
                SpreadsheetComparatorAliasSet.EMPTY
            ).set(
                SpreadsheetMetadataPropertyName.CONVERTERS,
                SpreadsheetConvertersConverterProviders.ALL.aliasSet()
            ).set(
                SpreadsheetMetadataPropertyName.EXPORTERS,
                SpreadsheetExporterAliasSet.EMPTY
            ).set(
                SpreadsheetMetadataPropertyName.FORM_HANDLERS,
                FormHandlerAliasSet.EMPTY
            ).set(
                SpreadsheetMetadataPropertyName.FORMATTERS,
                SpreadsheetFormatterAliasSet.EMPTY
            ).set(
                SpreadsheetMetadataPropertyName.FUNCTIONS,
                SpreadsheetExpressionFunctions.EMPTY_ALIAS_SET
            ).set(
                SpreadsheetMetadataPropertyName.IMPORTERS,
                SpreadsheetImporterAliasSet.EMPTY
            ).set(
                SpreadsheetMetadataPropertyName.PARSERS,
                SpreadsheetParserAliasSet.EMPTY
            ).set(
                SpreadsheetMetadataPropertyName.VALIDATORS,
                ValidatorAliasSet.EMPTY
            );
            final SpreadsheetMetadataStore metadataStore = SpreadsheetMetadataStores.treeMap();
            metadataStore.save(metadata);

            final SpreadsheetStoreRepository repo = SpreadsheetStoreRepositories.treeMap(metadataStore);

            return SpreadsheetEngineContexts.basic(
                SpreadsheetMetadataMode.SCRIPTING,
                SpreadsheetContexts.basic(
                    AbsoluteUrl.parseAbsolute("https://example.com"),
                    id,
                    (idid) -> repo,
                    SPREADSHEET_PROVIDER,
                    (c) -> SpreadsheetEngineContexts.basic(
                        SpreadsheetMetadataMode.FORMULA,
                        c,
                        TERMINAL_CONTEXT
                    ),
                    (SpreadsheetEngineContext c) -> new Router<>() {
                        @Override
                        public Optional<HttpHandler> route(final Map<HttpRequestAttribute<?>, Object> parameters) {
                            throw new UnsupportedOperationException();
                        }
                    },
                    EnvironmentContexts.map(ENVIRONMENT_CONTEXT),
                    LOCALE_CONTEXT,
                    PROVIDER_CONTEXT,
                    TERMINAL_SERVER_CONTEXT
                ),
                SpreadsheetMetadataTesting.TERMINAL_CONTEXT
            );
        }

        @Override
        public ProviderContext providerContext() {
            return SpreadsheetMetadataTesting.PROVIDER_CONTEXT;
        }

        @Override
        public SpreadsheetParser spreadsheetParser(final SpreadsheetParserSelector selector,
                                                   final ProviderContext context) {
            return SpreadsheetMetadataTesting.SPREADSHEET_PARSER_PROVIDER.spreadsheetParser(
                selector,
                context
            );
        }
    }

    // class............................................................................................................

    @Override
    public Class<SpreadsheetTerminalStorageSpreadsheetCell> type() {
        return SpreadsheetTerminalStorageSpreadsheetCell.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }
}
