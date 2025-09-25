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
import walkingkooka.locale.LocaleContext;
import walkingkooka.locale.LocaleContextDelegator;
import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.SpreadsheetContexts;
import walkingkooka.spreadsheet.SpreadsheetGlobalContext;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContexts;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStores;
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
import walkingkooka.spreadsheet.validation.form.store.SpreadsheetFormStores;
import walkingkooka.storage.Storages;
import walkingkooka.store.Store;

import java.util.List;
import java.util.Locale;
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
    public BasicSpreadsheetTerminalStorageContext createContext() {
        return BasicSpreadsheetTerminalStorageContext.with(
            SpreadsheetEngineContexts.basic(
                AbsoluteUrl.parseAbsolute("https://example.com"),
                METADATA_EN_AU.set(
                    SpreadsheetMetadataPropertyName.LOCALE,
                    LOCALE
                ),
                SpreadsheetMetadataPropertyName.SCRIPTING_FUNCTIONS,
                SpreadsheetContexts.basic(
                    SpreadsheetId.with(1),
                    (u, l) -> {
                        throw new UnsupportedOperationException();
                    },
                    SpreadsheetStoreRepositories.basic(
                        SpreadsheetCellStores.fake(),
                        SpreadsheetCellReferencesStores.fake(),
                        SpreadsheetColumnStores.fake(),
                        SpreadsheetFormStores.fake(),
                        SpreadsheetGroupStores.fake(),
                        SpreadsheetLabelStores.fake(),
                        SpreadsheetLabelReferencesStores.fake(),
                        SpreadsheetMetadataStores.treeMap(),
                        SpreadsheetCellRangeStores.fake(),
                        SpreadsheetCellRangeStores.fake(),
                        SpreadsheetRowStores.fake(),
                        Storages.fake(),
                        SpreadsheetUserStores.fake()
                    ),
                    SPREADSHEET_PROVIDER,
                    EnvironmentContexts.map(ENVIRONMENT_CONTEXT),
                    LOCALE_CONTEXT,
                    PROVIDER_CONTEXT
                ),
                SpreadsheetMetadataTesting.TERMINAL_CONTEXT
            ),
            TERMINAL_CONTEXT
        );
    }

    final static class TestSpreadsheetGlobalContext implements SpreadsheetGlobalContext,
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
        public SpreadsheetGlobalContext setLocale(final Locale locale) {
            return this;
        }

        @Override
        public LocaleContext localeContext() {
            return LOCALE_CONTEXT;
        }

        @Override
        public ProviderContext providerContext() {
            return PROVIDER_CONTEXT;
        }
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
