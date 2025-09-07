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
import walkingkooka.net.AbsoluteUrl;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContextDelegator;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContexts;
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
import walkingkooka.terminal.TerminalContext;
import walkingkooka.terminal.TerminalContextDelegator;

import java.time.LocalDateTime;
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
                    SpreadsheetMetadataStores.treeMap(
                        METADATA_EN_AU,
                        LocalDateTime::now
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
