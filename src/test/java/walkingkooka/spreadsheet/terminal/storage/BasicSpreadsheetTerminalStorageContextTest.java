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
import walkingkooka.environment.EnvironmentContexts;
import walkingkooka.io.TextReader;
import walkingkooka.io.TextReaders;
import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.route.Router;
import walkingkooka.spreadsheet.SpreadsheetContexts;
import walkingkooka.spreadsheet.compare.provider.SpreadsheetComparatorAliasSet;
import walkingkooka.spreadsheet.convert.provider.SpreadsheetConvertersConverterProviders;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContexts;
import walkingkooka.spreadsheet.engine.SpreadsheetMetadataMode;
import walkingkooka.spreadsheet.environment.SpreadsheetEnvironmentContext;
import walkingkooka.spreadsheet.environment.SpreadsheetEnvironmentContexts;
import walkingkooka.spreadsheet.export.provider.SpreadsheetExporterAliasSet;
import walkingkooka.spreadsheet.expression.SpreadsheetExpressionFunctions;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterAliasSet;
import walkingkooka.spreadsheet.importer.provider.SpreadsheetImporterAliasSet;
import walkingkooka.spreadsheet.meta.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStore;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStores;
import walkingkooka.spreadsheet.parser.provider.SpreadsheetParserAliasSet;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepositories;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.terminal.FakeTerminalContext;
import walkingkooka.text.printer.Printer;
import walkingkooka.text.printer.Printers;
import walkingkooka.validation.form.provider.FormHandlerAliasSet;
import walkingkooka.validation.provider.ValidatorAliasSet;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class BasicSpreadsheetTerminalStorageContextTest implements SpreadsheetTerminalStorageContextTesting<BasicSpreadsheetTerminalStorageContext>,
    SpreadsheetMetadataTesting {

    @Test
    public void testWithNullSpreadsheetEngineContext() {
        assertThrows(
            NullPointerException.class,
            () -> BasicSpreadsheetTerminalStorageContext.with(
                null,
                TERMINAL_CONTEXT
            )
        );
    }

    @Test
    public void testWithNullTerminalContext() {
        assertThrows(
            NullPointerException.class,
            () -> BasicSpreadsheetTerminalStorageContext.with(
                SpreadsheetEngineContexts.fake(),
                null
            )
        );
    }

    @Override
    public void testSetEnvironmentContextWithEqualEnvironmentContext() {
        throw new UnsupportedOperationException();
    }

    @Override
    public BasicSpreadsheetTerminalStorageContext createContext() {
        final SpreadsheetId spreadsheetId = SpreadsheetId.with(1);

        final SpreadsheetMetadata metadata = METADATA_EN_AU.set(
            SpreadsheetMetadataPropertyName.LOCALE,
            LOCALE
        ).set(
            SpreadsheetMetadataPropertyName.SPREADSHEET_ID,
            spreadsheetId
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

        return BasicSpreadsheetTerminalStorageContext.with(
            SpreadsheetEngineContexts.spreadsheetContext(
                SpreadsheetMetadataMode.SCRIPTING,
                SpreadsheetContexts.fixedSpreadsheetId(
                    repo,
                    (c) -> SpreadsheetEngineContexts.spreadsheetContext(
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
                    SpreadsheetEnvironmentContexts.basic(
                        EnvironmentContexts.map(SPREADSHEET_ENVIRONMENT_CONTEXT)
                            .setEnvironmentValue(
                                SpreadsheetEnvironmentContext.SPREADSHEET_ID,
                                spreadsheetId
                            )
                    ),
                    LOCALE_CONTEXT,
                    SPREADSHEET_PROVIDER,
                    PROVIDER_CONTEXT
                ),
                SpreadsheetMetadataTesting.TERMINAL_CONTEXT
            ),
            new FakeTerminalContext() {

                @Override
                public TextReader input() {
                    return TextReaders.fake();
                }

                @Override
                public Printer output() {
                    return Printers.fake();
                }

                @Override
                public Printer error() {
                    return Printers.fake();
                }

                @Override
                public Object evaluate(final String expression) {
                    Objects.requireNonNull(expression, "expression");
                    throw new UnsupportedOperationException();
                }
            }
        );
    }

    @Override
    public void testSetSpreadsheetIdWithSame() {
        throw new UnsupportedOperationException();
    }

    @Override
    public BasicSpreadsheetTerminalStorageContext createSpreadsheetProvider() {
        return this.createContext();
    }

    @Override
    public Class<BasicSpreadsheetTerminalStorageContext> type() {
        return BasicSpreadsheetTerminalStorageContext.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }
}
