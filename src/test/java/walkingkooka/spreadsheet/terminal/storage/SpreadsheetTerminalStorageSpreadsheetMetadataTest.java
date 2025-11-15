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
import walkingkooka.environment.EnvironmentValueName;
import walkingkooka.net.Url;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.route.Router;
import walkingkooka.spreadsheet.SpreadsheetContexts;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.SpreadsheetMediaTypes;
import walkingkooka.spreadsheet.SpreadsheetName;
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
import walkingkooka.storage.FakeStorageContext;
import walkingkooka.storage.StoragePath;
import walkingkooka.storage.StorageTesting;
import walkingkooka.storage.StorageValue;
import walkingkooka.storage.StorageValueInfo;
import walkingkooka.storage.Storages;
import walkingkooka.terminal.TerminalContext;
import walkingkooka.terminal.TerminalContextDelegator;
import walkingkooka.validation.form.provider.FormHandlerAliasSet;
import walkingkooka.validation.provider.ValidatorAliasSet;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetTerminalStorageSpreadsheetMetadataTest implements StorageTesting<SpreadsheetTerminalStorageSpreadsheetMetadata, SpreadsheetTerminalStorageContext>,
    SpreadsheetMetadataTesting {

    @Test
    public void testLoadMissingSpreadsheetMetadata() {
        final TestSpreadsheetTerminalStorageContext context = new TestSpreadsheetTerminalStorageContext();

        final StoragePath path = StoragePath.parse("/404");

        this.loadAndCheck(
            this.createStorage(),
            path,
            context
        );
    }

    @Test
    public void testLoadMissingSpreadsheetId() {
        final TestSpreadsheetTerminalStorageContext context = new TestSpreadsheetTerminalStorageContext();

        final SpreadsheetMetadata metadata = context.saveMetadata(METADATA_EN_AU);

        final StoragePath path = StoragePath.ROOT;

        this.loadAndCheck(
            this.createStorage(),
            path,
            context
        );
    }

    @Test
    public void testLoad() {
        final TestSpreadsheetTerminalStorageContext context = new TestSpreadsheetTerminalStorageContext();

        final SpreadsheetMetadata metadata = context.saveMetadata(METADATA_EN_AU);

        final StoragePath path = StoragePath.parse("/" + metadata.getOrFail(SpreadsheetMetadataPropertyName.SPREADSHEET_ID));

        this.loadAndCheck(
            this.createStorage(),
            path,
            context,
            StorageValue.with(
                path,
                Optional.of(metadata)
            ).setContentType(SpreadsheetMediaTypes.MEMORY_SPREADSHEET_METADATA)
        );
    }

    @Test
    public void testSaveWithStoragePathIncludingSpreadsheetIdFails() {
        final TestSpreadsheetTerminalStorageContext context = new TestSpreadsheetTerminalStorageContext();

        final IllegalArgumentException thrown = assertThrows(
            IllegalArgumentException.class,
            () -> this.createStorage()
                .save(
                    StorageValue.with(
                        StoragePath.parse("/1"),
                        Optional.of(METADATA_EN_AU)
                    ),
                    context
                )
        );

        this.checkEquals(
            "Invalid path, SpreadsheetId should not be present",
            thrown.getMessage()
        );
    }

    @Test
    public void testSavePathIncludesSpreadsheetId() {
        final IllegalArgumentException thrown = assertThrows(
            IllegalArgumentException.class,
            () -> this.createStorage()
                .save(
                    StorageValue.with(
                        StoragePath.parse("/1"),
                        Optional.of(
                            SpreadsheetMetadata.EMPTY
                        )
                    ),
                    new TestSpreadsheetTerminalStorageContext()
                )
        );

        this.checkEquals(
            "Invalid path, SpreadsheetId should not be present",
            thrown.getMessage()
        );
    }

    @Test
    public void testSaveWithStorageValueMissingSpreadsheetMetadataFails() {
        final TestSpreadsheetTerminalStorageContext context = new TestSpreadsheetTerminalStorageContext();

        final IllegalArgumentException thrown = assertThrows(
            IllegalArgumentException.class,
            () -> this.createStorage()
                .save(
                    StorageValue.with(
                        StoragePath.ROOT,
                        Optional.empty()
                    ),
                    context
                )
        );

        this.checkEquals(
            "Missing SpreadsheetMetadata",
            thrown.getMessage()
        );
    }

    @Test
    public void testSave() {
        final TestSpreadsheetTerminalStorageContext context = new TestSpreadsheetTerminalStorageContext();

        final SpreadsheetMetadata metadata = context.saveMetadata(METADATA_EN_AU);

        this.saveAndCheck(
            this.createStorage(),
            StorageValue.with(
                StoragePath.ROOT,
                Optional.of(metadata)
            ),
            context,
            StorageValue.with(
                StoragePath.parse("/" + metadata.getOrFail(SpreadsheetMetadataPropertyName.SPREADSHEET_ID)),
                Optional.of(metadata)
            ).setContentType(SpreadsheetMediaTypes.MEMORY_SPREADSHEET_METADATA)
        );
    }

    @Test
    public void testDelete() {
        final TestSpreadsheetTerminalStorageContext context = new TestSpreadsheetTerminalStorageContext();

        final SpreadsheetMetadata metadata = context.saveMetadata(METADATA_EN_AU);

        final SpreadsheetTerminalStorageSpreadsheetMetadata storage = this.createStorage();
        final StoragePath path = StoragePath.parse("/" + metadata.getOrFail(SpreadsheetMetadataPropertyName.SPREADSHEET_ID));

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
    public void testListMissingFilter() {
        final TestSpreadsheetTerminalStorageContext context = new TestSpreadsheetTerminalStorageContext();

        final SpreadsheetTerminalStorageSpreadsheetMetadata storage = this.createStorage();

        final StorageValue value1 = storage.save(
            StorageValue.with(
                StoragePath.ROOT,
                Optional.of(
                    METADATA_EN_AU.set(
                        SpreadsheetMetadataPropertyName.SPREADSHEET_NAME,
                        SpreadsheetName.with("Hello1")
                    )
                )
            ),
            context
        );

        final StorageValue value2 = storage.save(
            StorageValue.with(
                StoragePath.ROOT,
                Optional.of(
                    METADATA_EN_AU.set(
                        SpreadsheetMetadataPropertyName.SPREADSHEET_NAME,
                        SpreadsheetName.with("Hello2")
                    )
                )
            ),
            context
        );

        final StorageValue value3 = storage.save(
            StorageValue.with(
                StoragePath.ROOT,
                Optional.of(
                    METADATA_EN_AU.set(
                        SpreadsheetMetadataPropertyName.SPREADSHEET_NAME,
                        SpreadsheetName.with("Different3")
                    )
                )
            ),
            context
        );

        final StoragePath path = StoragePath.ROOT;

        this.listAndCheck(
            this.createStorage(),
            path,
            0,
            2,
            context,
            StorageValueInfo.with(
                StoragePath.parse(
                    "/" + (
                        (SpreadsheetMetadata) value1.value()
                            .get()
                    ).getOrFail(SpreadsheetMetadataPropertyName.SPREADSHEET_ID)
                ),
                context.createdAuditInfo()
            ),
            StorageValueInfo.with(
                StoragePath.parse(
                    "/" + (
                        (SpreadsheetMetadata) value2.value()
                            .get()
                    ).getOrFail(SpreadsheetMetadataPropertyName.SPREADSHEET_ID)
                ),
                context.createdAuditInfo()
            )
        );
    }

    @Test
    public void testList() {
        final TestSpreadsheetTerminalStorageContext context = new TestSpreadsheetTerminalStorageContext();

        final SpreadsheetTerminalStorageSpreadsheetMetadata storage = this.createStorage();

        final StorageValue value1 = storage.save(
            StorageValue.with(
                StoragePath.ROOT,
                Optional.of(
                    METADATA_EN_AU.set(
                        SpreadsheetMetadataPropertyName.SPREADSHEET_NAME,
                        SpreadsheetName.with("Hello1")
                    )
                )
            ),
            context
        );

        final StorageValue value2 = storage.save(
            StorageValue.with(
                StoragePath.ROOT,
                Optional.of(
                    METADATA_EN_AU.set(
                        SpreadsheetMetadataPropertyName.SPREADSHEET_NAME,
                        SpreadsheetName.with("Hello2")
                    )
                )
            ),
            context
        );

        final StorageValue value3 = storage.save(
            StorageValue.with(
                StoragePath.ROOT,
                Optional.of(
                    METADATA_EN_AU.set(
                        SpreadsheetMetadataPropertyName.SPREADSHEET_NAME,
                        SpreadsheetName.with("Different3")
                    )
                )
            ),
            context
        );

        final StoragePath path = StoragePath.parse("/Hello");

        this.listAndCheck(
            this.createStorage(),
            path,
            0,
            2,
            context,
            StorageValueInfo.with(
                StoragePath.parse(
                    "/" + (
                        (SpreadsheetMetadata) value1.value()
                            .get()
                    ).getOrFail(SpreadsheetMetadataPropertyName.SPREADSHEET_ID)
                ),
                context.createdAuditInfo()
            ),
            StorageValueInfo.with(
                StoragePath.parse(
                    "/" + (
                        (SpreadsheetMetadata) value2.value()
                            .get()
                    ).getOrFail(SpreadsheetMetadataPropertyName.SPREADSHEET_ID)
                ),
                context.createdAuditInfo()
            )
        );
    }

    @Override
    public SpreadsheetTerminalStorageSpreadsheetMetadata createStorage() {
        return SpreadsheetTerminalStorageSpreadsheetMetadata.INSTANCE;
    }

    @Override
    public SpreadsheetTerminalStorageContext createContext() {
        return SpreadsheetTerminalStorageContexts.fake();
    }

    static class TestSpreadsheetTerminalStorageContext extends FakeStorageContext implements SpreadsheetTerminalStorageContext,
        SpreadsheetEngineContextDelegator,
        TerminalContextDelegator {

        @Override
        public SpreadsheetEngineContext spreadsheetEngineContext() {
            return this.engineContext;
        }

        private final SpreadsheetEngineContext engineContext = createSpreadsheetEngineContext();

        private SpreadsheetEngineContext createSpreadsheetEngineContext() {
            final SpreadsheetId id = SpreadsheetId.with(1);
            final SpreadsheetMetadata metadata = METADATA_EN_AU.set(
                SpreadsheetMetadataPropertyName.LOCALE,
                SpreadsheetTerminalStorageSpreadsheetMetadataTest.LOCALE
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

            final SpreadsheetStoreRepository repo = SpreadsheetStoreRepositories.basic(
                SpreadsheetCellStores.treeMap(),
                SpreadsheetCellReferencesStores.treeMap(),
                SpreadsheetColumnStores.treeMap(),
                SpreadsheetFormStores.treeMap(),
                SpreadsheetGroupStores.treeMap(),
                SpreadsheetLabelStores.treeMap(),
                SpreadsheetLabelReferencesStores.treeMap(),
                metadataStore,
                SpreadsheetCellRangeStores.treeMap(),
                SpreadsheetRowStores.treeMap(),
                Storages.fake(),
                SpreadsheetUserStores.treeMap()
            );

            return SpreadsheetEngineContexts.basic(
                SpreadsheetEngineContextMode.SCRIPTING,
                SpreadsheetContexts.basic(
                    Url.parseAbsolute("https://example.com"),
                    id,
                    (idid) -> repo,
                    SPREADSHEET_PROVIDER,
                    (c) -> SpreadsheetEngineContexts.basic(
                        SpreadsheetEngineContextMode.FORMULA,
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
                    PROVIDER_CONTEXT
                ),
                SpreadsheetMetadataTesting.TERMINAL_CONTEXT
            );
        }

        @Override
        public SpreadsheetTerminalStorageContext setSpreadsheetId(final SpreadsheetId spreadsheetId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public TestSpreadsheetTerminalStorageContext cloneEnvironment() {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> TestSpreadsheetTerminalStorageContext setEnvironmentValue(final EnvironmentValueName<T> name,
                                                                             final T value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public TestSpreadsheetTerminalStorageContext removeEnvironmentValue(final EnvironmentValueName<?> name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public TestSpreadsheetTerminalStorageContext setLocale(final Locale locale) {
            throw new UnsupportedOperationException();
        }

        @Override
        public LocalDateTime now() {
            return NOW.now();
        }

        @Override
        public Optional<EmailAddress> user() {
            return this.spreadsheetEngineContext()
                .user();
        }

        @Override
        public TestSpreadsheetTerminalStorageContext setUser(final Optional<EmailAddress> user) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<SpreadsheetSelection> resolveLabel(final SpreadsheetLabelName label) {
            throw new UnsupportedOperationException();
        }

        @Override
        public TerminalContext terminalContext() {
            throw new UnsupportedOperationException();
        }

        @Override
        public TestSpreadsheetTerminalStorageContext exitTerminal() {
            throw new UnsupportedOperationException();
        }
    }

    // class............................................................................................................

    @Override
    public Class<SpreadsheetTerminalStorageSpreadsheetMetadata> type() {
        return SpreadsheetTerminalStorageSpreadsheetMetadata.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }
}
