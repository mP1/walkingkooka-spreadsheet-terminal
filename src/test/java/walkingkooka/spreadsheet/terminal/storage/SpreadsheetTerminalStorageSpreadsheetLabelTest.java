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
import walkingkooka.environment.EnvironmentContexts;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.route.Router;
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
import walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepositories;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.storage.StoragePath;
import walkingkooka.storage.StorageTesting;
import walkingkooka.storage.StorageValue;
import walkingkooka.storage.StorageValueInfo;
import walkingkooka.validation.form.provider.FormHandlerAliasSet;
import walkingkooka.validation.provider.ValidatorAliasSet;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetTerminalStorageSpreadsheetLabelTest implements StorageTesting<SpreadsheetTerminalStorageSpreadsheetLabel, SpreadsheetTerminalStorageContext>,
    SpreadsheetMetadataTesting {

    private final static SpreadsheetLabelName LABEL1 = SpreadsheetSelection.labelName("Label111");

    private final static SpreadsheetLabelMapping MAPPING1 = LABEL1.setLabelMappingReference(SpreadsheetSelection.A1);

    private final static SpreadsheetLabelName LABEL2 = SpreadsheetSelection.labelName("Label222");

    private final static SpreadsheetLabelMapping MAPPING2 = LABEL2.setLabelMappingReference(
        SpreadsheetSelection.parseCell("B2")
    );

    private final static SpreadsheetLabelName LABEL3 = SpreadsheetSelection.labelName("Label333");

    private final static SpreadsheetLabelMapping MAPPING3 = LABEL3.setLabelMappingReference(
        SpreadsheetSelection.parseCell("C3")
    );

    private final static StorageValueInfo INFO1 = StorageValueInfo.with(
        StoragePath.parse("/" + LABEL1),
        SPREADSHEET_ENVIRONMENT_CONTEXT.createdAuditInfo()
    );

    private final static StorageValueInfo INFO2 = StorageValueInfo.with(
        StoragePath.parse("/" + LABEL2),
        SPREADSHEET_ENVIRONMENT_CONTEXT.createdAuditInfo()
    );

    private final static StorageValueInfo INFO3 = StorageValueInfo.with(
        StoragePath.parse("/" + LABEL3),
        SPREADSHEET_ENVIRONMENT_CONTEXT.createdAuditInfo()
    );

    @Test
    public void testWithNullSpreadsheetEngineFails() {
        assertThrows(
            NullPointerException.class,
            () -> SpreadsheetTerminalStorageSpreadsheetLabel.with(null)
        );
    }

    @Test
    public void testLoadWithExtraPathFails() {
        final IllegalArgumentException thrown = assertThrows(
            IllegalArgumentException.class,
            () -> this.createStorage()
                .load(
                    StoragePath.parse("/" + LABEL1 + "/extra"),
                    new TestSpreadsheetTerminalStorageContext()
                )
        );

        this.checkEquals(
            "Invalid path after label name",
            thrown.getMessage()
        );
    }

    @Test
    public void testLoadMissingLabel() {
        final TestSpreadsheetTerminalStorageContext context = new TestSpreadsheetTerminalStorageContext();

        final StoragePath path = StoragePath.parse("/" + LABEL1);

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
                MAPPING1,
                context
            );

        final StoragePath path = StoragePath.parse("/" + LABEL1);

        this.loadAndCheck(
            this.createStorage(),
            path,
            context,
            StorageValue.with(
                path,
                Optional.of(
                    context.storeRepository()
                        .labels()
                        .loadOrFail(LABEL1)
                )
            ).setContentType(SpreadsheetMediaTypes.MEMORY_LABEL)
        );
    }

    @Test
    public void testSaveWithExtraPathFails() {
        final IllegalArgumentException thrown = assertThrows(
            IllegalArgumentException.class,
            () -> this.createStorage()
                .save(
                    StorageValue.with(
                        StoragePath.parse("/" + LABEL1 + "/extra"),
                        Optional.of(MAPPING1)
                    ),
                    new TestSpreadsheetTerminalStorageContext()
                )
        );

        this.checkEquals(
            "Invalid path after label",
            thrown.getMessage()
        );
    }

    @Test
    public void testSaveWithStorageValueMissingSpreadsheetLabelMapping() {
        final IllegalArgumentException thrown = assertThrows(
            IllegalArgumentException.class,
            () -> this.createStorage()
                .save(
                    StorageValue.with(
                        StoragePath.parse("/" + LABEL1),
                        Optional.empty()
                    ),
                    new TestSpreadsheetTerminalStorageContext()
                )
        );

        this.checkEquals(
            "Missing SpreadsheetLabelMapping",
            thrown.getMessage()
        );
    }

    @Test
    public void testSave() {
        final TestSpreadsheetTerminalStorageContext context = new TestSpreadsheetTerminalStorageContext();

        final StoragePath path = StoragePath.parse("/" + LABEL1);

        this.saveAndCheck(
            this.createStorage(),
            StorageValue.with(
                path,
                Optional.of(MAPPING1)
            ),
            context,
            StorageValue.with(
                path,
                Optional.of(MAPPING1)
            ).setContentType(SpreadsheetMediaTypes.MEMORY_LABEL)
        );
    }

    @Test
    public void testDeleteMissingLabelFails() {
        final IllegalArgumentException thrown = assertThrows(
            IllegalArgumentException.class,
            () -> this.createStorage()
                .delete(
                    StoragePath.ROOT,
                    new TestSpreadsheetTerminalStorageContext()
                )
        );

        this.checkEquals(
            "Missing label",
            thrown.getMessage()
        );
    }

    @Test
    public void testDeleteWithExtraPathFails() {
        final IllegalArgumentException thrown = assertThrows(
            IllegalArgumentException.class,
            () -> this.createStorage()
                .delete(
                    StoragePath.parse("/" + LABEL1 + "/extra"),
                    new TestSpreadsheetTerminalStorageContext()
                )
        );

        this.checkEquals(
            "Invalid path after label",
            thrown.getMessage()
        );
    }

    @Test
    public void testDelete() {
        final TestSpreadsheetTerminalStorageContext context = new TestSpreadsheetTerminalStorageContext();

        SpreadsheetEngines.basic()
            .saveLabel(
                MAPPING1,
                context
            );

        final StoragePath path = StoragePath.parse("/" + LABEL1);

        final SpreadsheetTerminalStorageSpreadsheetLabel storage = this.createStorage();
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
                    StoragePath.parse("/" + LABEL1 + "/extra"),
                    0,
                    1,
                    new TestSpreadsheetTerminalStorageContext()
                )
        );

        this.checkEquals(
            "Invalid path after label",
            thrown.getMessage()
        );
    }

    @Test
    public void testListMissingLabel() {
        final TestSpreadsheetTerminalStorageContext context = new TestSpreadsheetTerminalStorageContext();

        SpreadsheetEngines.basic()
            .saveLabel(
                MAPPING1,
                context
            );

        SpreadsheetEngines.basic()
            .saveLabel(
                MAPPING2,
                context
            );

        SpreadsheetEngines.basic()
            .saveLabel(
                MAPPING3,
                context
            );

        this.listAndCheck(
            this.createStorage(),
            StoragePath.ROOT,
            0,
            4,
            context,
            INFO1,
            INFO2,
            INFO3
        );
    }

    @Test
    public void testListMissingLabelWithOffset() {
        final TestSpreadsheetTerminalStorageContext context = new TestSpreadsheetTerminalStorageContext();

        SpreadsheetEngines.basic()
            .saveLabel(
                MAPPING1,
                context
            );

        SpreadsheetEngines.basic()
            .saveLabel(
                MAPPING2,
                context
            );

        SpreadsheetEngines.basic()
            .saveLabel(
                MAPPING3,
                context
            );

        this.listAndCheck(
            this.createStorage(),
            StoragePath.ROOT,
            1,
            4,
            context,
            INFO2,
            INFO3
        );
    }

    @Test
    public void testListMissingLabelWithCount() {
        final TestSpreadsheetTerminalStorageContext context = new TestSpreadsheetTerminalStorageContext();

        SpreadsheetEngines.basic()
            .saveLabel(
                MAPPING1,
                context
            );

        SpreadsheetEngines.basic()
            .saveLabel(
                MAPPING2,
                context
            );

        SpreadsheetEngines.basic()
            .saveLabel(
                MAPPING3,
                context
            );

        this.listAndCheck(
            this.createStorage(),
            StoragePath.ROOT,
            0,
            2,
            context,
            INFO1,
            INFO2
        );
    }

    @Test
    public void testListWithPrefix() {
        final TestSpreadsheetTerminalStorageContext context = new TestSpreadsheetTerminalStorageContext();

        SpreadsheetEngines.basic()
            .saveLabel(
                MAPPING1,
                context
            );

        SpreadsheetEngines.basic()
            .saveLabel(
                MAPPING2,
                context
            );

        SpreadsheetEngines.basic()
            .saveLabel(
                MAPPING3,
                context
            );

        SpreadsheetEngines.basic()
            .saveLabel(
                MAPPING1,
                context
            );

        this.listAndCheck(
            this.createStorage(),
            StoragePath.parse("/Label"),
            0,
            2,
            context,
            INFO1,
            INFO2
        );
    }

    @Override
    public SpreadsheetTerminalStorageSpreadsheetLabel createStorage() {
        return SpreadsheetTerminalStorageSpreadsheetLabel.with(SpreadsheetEngines.basic());
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

        private final SpreadsheetEngineContext engineContext = createSpreadsheetEngineContext();

        private SpreadsheetEngineContext createSpreadsheetEngineContext() {
            final SpreadsheetId id = SpreadsheetId.with(1);
            final SpreadsheetMetadata metadata = METADATA_EN_AU.set(
                SpreadsheetMetadataPropertyName.LOCALE,
                SpreadsheetTerminalStorageSpreadsheetLabelTest.LOCALE
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

            return SpreadsheetEngineContexts.spreadsheetContext(
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
                                id
                            )
                    ),
                    LOCALE_CONTEXT,
                    SPREADSHEET_PROVIDER,
                    PROVIDER_CONTEXT
                ),
                SpreadsheetMetadataTesting.TERMINAL_CONTEXT
            );
        }
    }

    // class............................................................................................................

    @Override
    public Class<SpreadsheetTerminalStorageSpreadsheetLabel> type() {
        return SpreadsheetTerminalStorageSpreadsheetLabel.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }
}
