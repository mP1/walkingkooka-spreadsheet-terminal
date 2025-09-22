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
import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.SpreadsheetMediaTypes;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContextDelegator;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContexts;
import walkingkooka.spreadsheet.engine.SpreadsheetEngines;
import walkingkooka.spreadsheet.meta.FakeSpreadsheetContext;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStores;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping;
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
import walkingkooka.storage.StoragePath;
import walkingkooka.storage.StorageTesting;
import walkingkooka.storage.StorageValue;
import walkingkooka.storage.StorageValueInfo;
import walkingkooka.storage.Storages;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetTerminalSpreadsheetLabelStorageTest implements StorageTesting<SpreadsheetTerminalSpreadsheetLabelStorage, SpreadsheetTerminalStorageContext>,
    SpreadsheetMetadataTesting {

    private final static SpreadsheetLabelName LABEL = SpreadsheetSelection.labelName("Label123");

    private final static SpreadsheetLabelMapping MAPPING = LABEL.setLabelMappingReference(SpreadsheetSelection.A1);

    @Test
    public void testWithNullSpreadsheetEngineFails() {
        assertThrows(
            NullPointerException.class,
            () -> SpreadsheetTerminalSpreadsheetLabelStorage.with(null)
        );
    }

    @Test
    public void testLoadMissingLabel() {
        final TestSpreadsheetTerminalStorageContext context = new TestSpreadsheetTerminalStorageContext();

        final StoragePath path = StoragePath.parse("/" + LABEL);

        this.loadAndCheck(
            this.createStorage(),
            path,
            context
        );
    }

    @Test
    public void testLoad() {
        final TestSpreadsheetTerminalStorageContext context = new TestSpreadsheetTerminalStorageContext();

        SpreadsheetEngines.basic()
            .saveLabel(
                MAPPING,
                context
            );

        final StoragePath path = StoragePath.parse("/" + LABEL);

        this.loadAndCheck(
            this.createStorage(),
            path,
            context,
            StorageValue.with(
                path,
                Optional.of(
                    Sets.of(
                        context.storeRepository()
                            .labels()
                            .loadOrFail(LABEL)
                    )
                )
            ).setContentType(SpreadsheetMediaTypes.MEMORY_LABEL)
        );
    }

    @Test
    public void testSave() {
        final TestSpreadsheetTerminalStorageContext context = new TestSpreadsheetTerminalStorageContext();

        final StoragePath path = StoragePath.parse("/" + LABEL);

        this.saveAndCheck(
            this.createStorage(),
            StorageValue.with(
                path,
                Optional.of(MAPPING)
            ),
            context,
            StorageValue.with(
                path,
                Optional.of(
                    Sets.of(MAPPING)
                )
            ).setContentType(SpreadsheetMediaTypes.MEMORY_LABEL)
        );
    }

    @Test
    public void testDelete() {
        final TestSpreadsheetTerminalStorageContext context = new TestSpreadsheetTerminalStorageContext();

        SpreadsheetEngines.basic()
            .saveLabel(
                MAPPING,
                context
            );

        final StoragePath path = StoragePath.parse("/" + LABEL);

        final SpreadsheetTerminalSpreadsheetLabelStorage storage = this.createStorage();
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
    public void testList() {
        final TestSpreadsheetTerminalStorageContext context = new TestSpreadsheetTerminalStorageContext();

        SpreadsheetEngines.basic()
            .saveLabel(
                MAPPING,
                context
            );

        final StoragePath path = StoragePath.parse("/" + LABEL);

        this.listAndCheck(
            this.createStorage(),
            path,
            0,
            2,
            context,
            StorageValueInfo.with(
                StoragePath.parse("/" + LABEL),
                context.createdAuditInfo()
            )
        );
    }

    @Override
    public SpreadsheetTerminalSpreadsheetLabelStorage createStorage() {
        return SpreadsheetTerminalSpreadsheetLabelStorage.with(SpreadsheetEngines.basic());
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
        public SpreadsheetMetadata spreadsheetMetadata() {
            return this.engineContext.spreadsheetMetadata();
        }

        @Override
        public <T> Either<T, String> convert(Object o, Class<T> aClass) {
            return this.engineContext.convert(o, aClass);
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
                new FakeSpreadsheetContext() {
                    @Override
                    public ProviderContext providerContext() {
                        return PROVIDER_CONTEXT;
                    }
                },
                SpreadsheetMetadataTesting.TERMINAL_CONTEXT,
                SpreadsheetMetadataTesting.SPREADSHEET_PROVIDER
            ),
            TERMINAL_CONTEXT
        );
    }

    // class............................................................................................................

    @Override
    public Class<SpreadsheetTerminalSpreadsheetLabelStorage> type() {
        return SpreadsheetTerminalSpreadsheetLabelStorage.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }
}
