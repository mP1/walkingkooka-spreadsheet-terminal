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

import walkingkooka.environment.EnvironmentContext;
import walkingkooka.environment.EnvironmentValueName;
import walkingkooka.io.TextReader;
import walkingkooka.io.TextReaders;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.route.Router;
import walkingkooka.spreadsheet.SpreadsheetContexts;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.compare.provider.SpreadsheetComparatorAliasSet;
import walkingkooka.spreadsheet.convert.provider.SpreadsheetConvertersConverterProviders;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContextDelegator;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContexts;
import walkingkooka.spreadsheet.engine.SpreadsheetMetadataMode;
import walkingkooka.spreadsheet.environment.SpreadsheetEnvironmentContext;
import walkingkooka.spreadsheet.environment.SpreadsheetEnvironmentContexts;
import walkingkooka.spreadsheet.export.provider.SpreadsheetExporterAliasSet;
import walkingkooka.spreadsheet.expression.SpreadsheetExpressionFunctions;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterAliasSet;
import walkingkooka.spreadsheet.importer.provider.SpreadsheetImporterAliasSet;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStore;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStores;
import walkingkooka.spreadsheet.parser.provider.SpreadsheetParserAliasSet;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelNameResolvers;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepositories;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.spreadsheet.terminal.storage.SpreadsheetTerminalStorageContextTestingTest.TestSpreadsheetTerminalStorageContext;
import walkingkooka.terminal.FakeTerminalContext;
import walkingkooka.terminal.TerminalContext;
import walkingkooka.terminal.TerminalContextDelegator;
import walkingkooka.text.LineEnding;
import walkingkooka.text.printer.Printer;
import walkingkooka.text.printer.Printers;
import walkingkooka.validation.form.provider.FormHandlerAliasSet;
import walkingkooka.validation.provider.ValidatorAliasSet;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class SpreadsheetTerminalStorageContextTestingTest implements SpreadsheetTerminalStorageContextTesting<TestSpreadsheetTerminalStorageContext>,
    SpreadsheetMetadataTesting {

    private final static SpreadsheetId SPREADSHEET_ID = SpreadsheetId.with(1);

    @Override
    public void testTestNaming() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TestSpreadsheetTerminalStorageContext createContext() {
        return new TestSpreadsheetTerminalStorageContext();
    }

    @Override
    public TestSpreadsheetTerminalStorageContext createSpreadsheetProvider() {
        return this.createContext();
    }

    @Override
    public Class<TestSpreadsheetTerminalStorageContext> type() {
        return TestSpreadsheetTerminalStorageContext.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }

    final static class TestSpreadsheetTerminalStorageContext implements SpreadsheetTerminalStorageContext,
        SpreadsheetEngineContextDelegator,
        TerminalContextDelegator {

        @Override
        public Optional<SpreadsheetSelection> resolveLabel(final SpreadsheetLabelName labelName) {
            return SpreadsheetLabelNameResolvers.empty()
                .resolveLabel(labelName);
        }

        // EnvironmentContextDelegator..................................................................................

        @Override
        public SpreadsheetEngineContext environmentContext() {
            return this.spreadsheetEngineContext();
        }

        @Override
        public SpreadsheetTerminalStorageContext cloneEnvironment() {
            throw new UnsupportedOperationException();
        }

        @Override
        public SpreadsheetTerminalStorageContext setEnvironmentContext(final EnvironmentContext environmentContext) {
            Objects.requireNonNull(environmentContext, "environmentContext");

            return new TestSpreadsheetTerminalStorageContext();
        }

        @Override
        public <T> SpreadsheetTerminalStorageContext setEnvironmentValue(final EnvironmentValueName<T> name,
                                                                         final T reference) {
            this.spreadsheetEngineContext()
                .setEnvironmentValue(name, reference);
            return this;
        }

        @Override
        public SpreadsheetTerminalStorageContext removeEnvironmentValue(final EnvironmentValueName<?> name) {
            this.spreadsheetEngineContext()
                .removeEnvironmentValue(name);
            return this;
        }

        @Override
        public SpreadsheetTerminalStorageContext setLineEnding(final LineEnding lineEnding) {
            this.spreadsheetEngineContext()
                .setLineEnding(lineEnding);
            return this;
        }
        
        @Override
        public SpreadsheetTerminalStorageContext setLocale(final Locale locale) {
            this.spreadsheetEngineContext()
                .setLocale(locale);
            return this;
        }

        @Override
        public Optional<EmailAddress> user() {
            return this.spreadsheetEngineContext()
                .user();
        }

        @Override
        public SpreadsheetTerminalStorageContext setUser(final Optional<EmailAddress> user) {
            this.spreadsheetEngineContext()
                .setUser(user);
            return this;
        }

        // SpreadsheetEngineContextDelegator............................................................................

        @Override
        public SpreadsheetTerminalStorageContext setSpreadsheetId(final SpreadsheetId spreadsheetId) {
            Objects.requireNonNull(spreadsheetId, "spreadsheetId");

            if (SpreadsheetTerminalStorageContextTestingTest.SPREADSHEET_ID.equals(spreadsheetId)) {
                return this;
            }
            throw new UnsupportedOperationException();
        }

        @Override
        public SpreadsheetEngineContext spreadsheetEngineContext() {
            if (null == this.spreadsheetEngineContext) {
                final SpreadsheetMetadata metadata = METADATA_EN_AU.set(
                    SpreadsheetMetadataPropertyName.LOCALE,
                    SpreadsheetTerminalStorageSpreadsheetCellTest.LOCALE
                ).set(
                    SpreadsheetMetadataPropertyName.SPREADSHEET_ID,
                    SpreadsheetTerminalStorageContextTestingTest.SPREADSHEET_ID
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

                this.spreadsheetEngineContext = SpreadsheetEngineContexts.spreadsheetContext(
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
                            SPREADSHEET_ENVIRONMENT_CONTEXT.cloneEnvironment()
                                .setEnvironmentValue(
                                    SpreadsheetEnvironmentContext.SPREADSHEET_ID,
                                    SpreadsheetTerminalStorageContextTestingTest.SPREADSHEET_ID
                                )
                        ),
                        LOCALE_CONTEXT,
                        SPREADSHEET_PROVIDER,
                        PROVIDER_CONTEXT
                    ),
                    SpreadsheetMetadataTesting.TERMINAL_CONTEXT
                );
            }

            return this.spreadsheetEngineContext;
        }

        private SpreadsheetEngineContext spreadsheetEngineContext;

        // TerminalContextDelegator.....................................................................................

        @Override
        public SpreadsheetTerminalStorageContext exitTerminal() {
            TERMINAL_CONTEXT.exitTerminal();
            return this;
        }

        @Override
        public Object evaluate(final String expression) {
            return TERMINAL_CONTEXT.evaluate(expression);
        }

        @Override
        public TerminalContext terminalContext() {
            return new FakeTerminalContext() {

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
            };
        }

        // toString.....................................................................................................

        @Override
        public String toString() {
            return this.getClass().getSimpleName();
        }
    }
}
