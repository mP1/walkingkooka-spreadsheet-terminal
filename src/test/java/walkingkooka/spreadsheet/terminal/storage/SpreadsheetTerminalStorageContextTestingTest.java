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
import walkingkooka.locale.LocaleContext;
import walkingkooka.locale.LocaleContextDelegator;
import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContextDelegator;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContexts;
import walkingkooka.spreadsheet.meta.SpreadsheetContext;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStores;
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
import walkingkooka.store.Store;
import walkingkooka.terminal.TerminalContext;
import walkingkooka.terminal.TerminalContextDelegator;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
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
            ENVIRONMENT_CONTEXT.setEnvironmentValue(name, reference);
            throw new UnsupportedOperationException();
        }

        @Override
        public SpreadsheetTerminalStorageContext removeEnvironmentValue(final EnvironmentValueName<?> name) {
            ENVIRONMENT_CONTEXT.removeEnvironmentValue(name);
            throw new UnsupportedOperationException();
        }

        @Override
        public SpreadsheetTerminalStorageContext setLocale(final Locale locale) {
            ENVIRONMENT_CONTEXT.setLocale(locale);
            throw new UnsupportedOperationException();
        }

        // SpreadsheetEngineContextDelegator............................................................................

        @Override
        public SpreadsheetEngineContext spreadsheetEngineContext() {
            return SpreadsheetEngineContexts.basic(
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
                    SpreadsheetMetadataStores.treeMap(),
                    SpreadsheetCellRangeStores.treeMap(),
                    SpreadsheetCellRangeStores.treeMap(),
                    SpreadsheetRowStores.treeMap(),
                    Storages.tree(),
                    SpreadsheetUserStores.treeMap()
                ),
                SpreadsheetMetadataPropertyName.SCRIPTING_FUNCTIONS,
                SpreadsheetMetadataTesting.ENVIRONMENT_CONTEXT,
                new TestSpreadsheetContext(),
                SpreadsheetMetadataTesting.TERMINAL_CONTEXT,
                SpreadsheetMetadataTesting.SPREADSHEET_PROVIDER
            );
        }

        final static class TestSpreadsheetContext implements SpreadsheetContext,
            LocaleContextDelegator {

            @Override
            public SpreadsheetMetadata createMetadata(final EmailAddress user,
                                                      final Optional<Locale> locale) {
                Objects.requireNonNull(user, "user");
                Objects.requireNonNull(locale, "locale");

                throw new UnsupportedOperationException();
            }

            @Override
            public Optional<SpreadsheetMetadata> loadMetadata(final SpreadsheetId id) {
                Objects.requireNonNull(id, "id");

                throw new UnsupportedOperationException();
            }

            @Override
            public SpreadsheetMetadata saveMetadata(final SpreadsheetMetadata metadata) {
                Objects.requireNonNull(metadata, "metadata");

                throw new UnsupportedOperationException();
            }

            @Override
            public void deleteMetadata(final SpreadsheetId id) {
                Objects.requireNonNull(id, "id");

                throw new UnsupportedOperationException();
            }

            @Override
            public List<SpreadsheetMetadata> findMetadataBySpreadsheetName(final String name,
                                                                           final int offset,
                                                                           final int count) {
                Objects.requireNonNull(name, "name");
                Store.checkOffsetAndCount(offset, count);

                throw new UnsupportedOperationException();
            }

            @Override
            public LocaleContext localeContext() {
                return LOCALE_CONTEXT;
            }

            @Override
            public SpreadsheetContext setLocale(final Locale locale) {
                return this;
            }

            @Override
            public ProviderContext providerContext() {
                return PROVIDER_CONTEXT;
            }
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
