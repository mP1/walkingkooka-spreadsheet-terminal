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
import walkingkooka.environment.EnvironmentContexts;
import walkingkooka.environment.EnvironmentValueName;
import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.SpreadsheetContexts;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.compare.provider.SpreadsheetComparatorAliasSet;
import walkingkooka.spreadsheet.convert.provider.SpreadsheetConvertersConverterProviders;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContextDelegator;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContextMode;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContexts;
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
import walkingkooka.spreadsheet.terminal.storage.SpreadsheetTerminalStorageContextTestingTest.TestSpreadsheetTerminalStorageContext;
import walkingkooka.spreadsheet.validation.form.store.SpreadsheetFormStores;
import walkingkooka.storage.Storages;
import walkingkooka.terminal.TerminalContext;
import walkingkooka.terminal.TerminalContextDelegator;
import walkingkooka.validation.form.provider.FormHandlerAliasSet;
import walkingkooka.validation.provider.ValidatorAliasSet;

import java.util.Locale;
import java.util.Optional;

public final class SpreadsheetTerminalStorageContextTestingTest implements SpreadsheetTerminalStorageContextTesting<TestSpreadsheetTerminalStorageContext>,
    SpreadsheetMetadataTesting {

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
        public EnvironmentContext environmentContext() {
            return ENVIRONMENT_CONTEXT;
        }

        @Override
        public SpreadsheetTerminalStorageContext cloneEnvironment() {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> SpreadsheetTerminalStorageContext setEnvironmentValue(final EnvironmentValueName<T> name,
                                                                         final T reference) {
            this.environmentContext.setEnvironmentValue(name, reference);
            return this;
        }

        @Override
        public SpreadsheetTerminalStorageContext removeEnvironmentValue(final EnvironmentValueName<?> name) {
            this.environmentContext.removeEnvironmentValue(name);
            return this;
        }

        @Override
        public SpreadsheetTerminalStorageContext setLocale(final Locale locale) {
            this.environmentContext.setLocale(locale);
            return this;
        }

        @Override
        public SpreadsheetTerminalStorageContext setUser(final Optional<EmailAddress> user) {
            this.environmentContext.setUser(user);
            return this;
        }

        private final EnvironmentContext environmentContext = EnvironmentContexts.map(ENVIRONMENT_CONTEXT);

        // SpreadsheetEngineContextDelegator............................................................................

        @Override
        public SpreadsheetEngineContext spreadsheetEngineContext() {
            final SpreadsheetId id = SpreadsheetId.with(1);
            final SpreadsheetMetadata metadata = METADATA_EN_AU.set(
                SpreadsheetMetadataPropertyName.LOCALE,
                LOCALE
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

            return SpreadsheetEngineContexts.basic(
                SpreadsheetEngineContextMode.SCRIPTING,
                SpreadsheetContexts.basic(
                    AbsoluteUrl.parseAbsolute("https://example.com"),
                    id,
                    SpreadsheetStoreRepositories.basic(
                        SpreadsheetCellStores.treeMap(),
                        SpreadsheetCellReferencesStores.treeMap(),
                        SpreadsheetColumnStores.treeMap(),
                        SpreadsheetFormStores.treeMap(),
                        SpreadsheetGroupStores.treeMap(),
                        SpreadsheetLabelStores.treeMap(),
                        SpreadsheetLabelReferencesStores.treeMap(),
                        metadataStore,
                        SpreadsheetCellRangeStores.treeMap(),
                        SpreadsheetCellRangeStores.treeMap(),
                        SpreadsheetRowStores.treeMap(),
                        Storages.fake(),
                        SpreadsheetUserStores.treeMap()
                    ),
                    SPREADSHEET_PROVIDER,
                    (c) -> SpreadsheetEngineContexts.basic(
                        SpreadsheetEngineContextMode.FORMULA,
                        c,
                        TERMINAL_CONTEXT
                    ),
                    EnvironmentContexts.map(ENVIRONMENT_CONTEXT),
                    LOCALE_CONTEXT,
                    PROVIDER_CONTEXT
                ),
                SpreadsheetMetadataTesting.TERMINAL_CONTEXT
            );
        }

        // TerminalContextDelegator.....................................................................................

        @Override
        public TerminalContext terminalContext() {
            return TERMINAL_CONTEXT;
        }

        // toString.....................................................................................................

        @Override
        public String toString() {
            return this.getClass().getSimpleName();
        }
    }
}
