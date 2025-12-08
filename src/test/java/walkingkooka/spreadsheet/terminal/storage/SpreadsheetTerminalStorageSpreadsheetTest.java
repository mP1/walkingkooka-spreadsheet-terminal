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
import walkingkooka.Cast;
import walkingkooka.ToStringTesting;
import walkingkooka.collect.map.Maps;
import walkingkooka.environment.AuditInfo;
import walkingkooka.environment.EnvironmentContext;
import walkingkooka.environment.EnvironmentContexts;
import walkingkooka.environment.EnvironmentValueName;
import walkingkooka.environment.FakeEnvironmentContext;
import walkingkooka.net.Url;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.route.Router;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetContexts;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.SpreadsheetMediaTypes;
import walkingkooka.spreadsheet.SpreadsheetName;
import walkingkooka.spreadsheet.compare.provider.SpreadsheetComparatorAliasSet;
import walkingkooka.spreadsheet.convert.provider.SpreadsheetConvertersConverterProviders;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
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
import walkingkooka.spreadsheet.formula.SpreadsheetFormula;
import walkingkooka.spreadsheet.importer.provider.SpreadsheetImporterAliasSet;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStore;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStores;
import walkingkooka.spreadsheet.parser.provider.SpreadsheetParserAliasSet;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepositories;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.storage.FakeStorageContext;
import walkingkooka.storage.Storage;
import walkingkooka.storage.StoragePath;
import walkingkooka.storage.StorageTesting;
import walkingkooka.storage.StorageValue;
import walkingkooka.storage.StorageValueInfo;
import walkingkooka.storage.Storages;
import walkingkooka.terminal.TerminalContext;
import walkingkooka.terminal.TerminalContextDelegator;
import walkingkooka.text.LineEnding;
import walkingkooka.tree.text.TextNode;
import walkingkooka.validation.form.provider.FormHandlerAliasSet;
import walkingkooka.validation.provider.ValidatorAliasSet;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetTerminalStorageSpreadsheetTest implements StorageTesting<SpreadsheetTerminalStorageSpreadsheet, SpreadsheetTerminalStorageContext>,
    ToStringTesting<SpreadsheetTerminalStorageSpreadsheet>,
    SpreadsheetMetadataTesting {

    private final static SpreadsheetId SPREADSHEET_ID1 = SpreadsheetId.with(0x111);

    private final static SpreadsheetId SPREADSHEET_ID2 = SpreadsheetId.with(0x222);

    private final static SpreadsheetMetadata METADATA1 = METADATA_EN_AU.set(
        SpreadsheetMetadataPropertyName.LOCALE,
        LOCALE
    ).set(
        SpreadsheetMetadataPropertyName.SPREADSHEET_ID,
        SPREADSHEET_ID1
    ).set(
        SpreadsheetMetadataPropertyName.SPREADSHEET_NAME,
        SpreadsheetName.with("Spreadsheet111")
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

    private final static SpreadsheetMetadata METADATA2 = METADATA1.set(
        SpreadsheetMetadataPropertyName.SPREADSHEET_ID,
        SPREADSHEET_ID2
    ).set(
        SpreadsheetMetadataPropertyName.SPREADSHEET_NAME,
        SpreadsheetName.with("Spreadsheet222")
    );

    private final Storage<SpreadsheetTerminalStorageContext> CELLS = Storages.fake();
    private final Storage<SpreadsheetTerminalStorageContext> LABELS = Storages.fake();
    private final Storage<SpreadsheetTerminalStorageContext> METADATAS = Storages.fake();

    private final static EnvironmentContext ENVIRONMENT_CONTEXT = new FakeEnvironmentContext() {
        @Override
        public <T> Optional<T> environmentValue(final EnvironmentValueName<T> name) {
            return Optional.ofNullable(
                SpreadsheetTerminalStorageSpreadsheet.SPREADSHEET_ID.equals(name) ?
                    Cast.to(SPREADSHEET_ID1) :
                    null
            );
        }

        @Override
        public Optional<EmailAddress> user() {
            return Optional.of(SpreadsheetTerminalStorageSpreadsheetTest.USER);
        }

        @Override
        public LocalDateTime now() {
            return HAS_NOW.now();
        }
    };

    private final static SpreadsheetCell CELL1 = SpreadsheetSelection.A1.setFormula(
        SpreadsheetFormula.EMPTY.setValue(
            Optional.of(111)
        )
    );

    private final static SpreadsheetCell CELL2 = SpreadsheetSelection.parseCell("b2")
        .setFormula(
            SpreadsheetFormula.EMPTY.setValue(
                Optional.of(222)
            )
        );

    private final static SpreadsheetLabelName LABEL1 = SpreadsheetSelection.labelName("Label111");

    private final static SpreadsheetLabelName LABEL2 = SpreadsheetSelection.labelName("Label222");

    private final static SpreadsheetLabelMapping MAPPING1 = LABEL1.setLabelMappingReference(
        SpreadsheetSelection.labelName("Target111")
    );

    private final static SpreadsheetLabelMapping MAPPING2 = LABEL2.setLabelMappingReference(
        SpreadsheetSelection.labelName("Target222")
    );

    private final static AuditInfo AUDIT_INFO = ENVIRONMENT_CONTEXT.createdAuditInfo();

    private static final StorageValueInfo METADATA_INFO1 = StorageValueInfo.with(
        StoragePath.parse("/spreadsheet/111"),
        AUDIT_INFO
    );

    private static final StorageValueInfo METADATA_INFO2 = StorageValueInfo.with(
        StoragePath.parse("/spreadsheet/222"),
        AUDIT_INFO
    );

    private static final SpreadsheetCellReference DIFFERENT_CELL_REFERENCE = SpreadsheetSelection.parseCell("C3");

    private static final SpreadsheetCell DIFFERENT_UNFORMATTED_CELL = DIFFERENT_CELL_REFERENCE.setFormula(
        SpreadsheetFormula.EMPTY.setValue(
            Optional.of(999)
        )
    );

    private static final SpreadsheetCell DIFFERENT_FORMATTED_CELL = DIFFERENT_UNFORMATTED_CELL.setFormattedValue(
        Optional.of(
            TextNode.text("999.")
        )
    );

    private static final SpreadsheetLabelMapping DIFFERENT_MAPPING = SpreadsheetSelection.labelName("DifferentLabel")
        .setLabelMappingReference(DIFFERENT_CELL_REFERENCE);

    // with.............................................................................................................

    @Test
    public void testWithNullCellsFails() {
        assertThrows(
            NullPointerException.class,
            () -> SpreadsheetTerminalStorageSpreadsheet.with(
                null,
                LABELS,
                METADATAS
            )
        );
    }

    @Test
    public void testWithNullLabelsFails() {
        assertThrows(
            NullPointerException.class,
            () -> SpreadsheetTerminalStorageSpreadsheet.with(
                CELLS,
                null,
                METADATAS
            )
        );
    }

    @Test
    public void testWithNullMetadataFails() {
        assertThrows(
            NullPointerException.class,
            () -> SpreadsheetTerminalStorageSpreadsheet.with(
                CELLS,
                LABELS,
                null
            )
        );
    }

    // Storage.load.....................................................................................................

    @Test
    public void testLoadWithoutSpreadsheet() {
        final StoragePath path = StoragePath.parse("/spreadsheet");

        this.loadAndCheck(
            this.createStorage(),
            path,
            this.createContext()
        );
    }

    @Test
    public void testLoadWithUnknownSpreadsheetId() {
        this.loadAndCheck(
            this.createStorage(),
            StoragePath.parse("/spreadsheet/404"),
            this.createContext()
        );
    }

    @Test
    public void testLoadWithSpreadsheetId1() {
        final StoragePath path = StoragePath.parse("/spreadsheet/111");

        this.loadAndCheck(
            this.createStorage(),
            path,
            this.createContext(),
            StorageValue.with(
                path,
                Optional.of(METADATA1)
            ).setContentType(SpreadsheetMediaTypes.MEMORY_SPREADSHEET_METADATA)
        );
    }

    @Test
    public void testLoadWithSpreadsheetId2() {
        final StoragePath path = StoragePath.parse("/spreadsheet/222");

        this.loadAndCheck(
            this.createStorage(),
            path,
            this.createContext(),
            StorageValue.with(
                path,
                Optional.of(METADATA2)
            ).setContentType(
                SpreadsheetMediaTypes.MEMORY_SPREADSHEET_METADATA
            )
        );
    }

    @Test
    public void testLoadWithCell() {
        final StoragePath path = StoragePath.parse("/cell/A1");

        this.loadAndCheck(
            this.createStorage(),
            path,
            this.createContext(),
            StorageValue.with(
                path,
                Optional.of(
                    CELL1.setFormattedValue(
                        Optional.of(
                            TextNode.text("111.")
                        )
                    )
                )
            ).setContentType(
                SpreadsheetMediaTypes.MEMORY_CELL
            )
        );
    }

    @Test
    public void testLoadWithCell2() {
        final StoragePath path = StoragePath.parse("/cell/a1");

        this.loadAndCheck(
            this.createStorage(),
            path,
            this.createContext(),
            StorageValue.with(
                path,
                Optional.of(
                    CELL1.setFormattedValue(
                        Optional.of(
                            TextNode.text("111.")
                        )
                    )
                )
            ).setContentType(
                SpreadsheetMediaTypes.MEMORY_CELL
            )
        );
    }

    @Test
    public void testLoadWithUnknownCell() {
        this.loadAndCheck(
            this.createStorage(),
            StoragePath.parse("/cell/Z999"),
            this.createContext()
        );
    }

    @Test
    public void testLoadWithLabel() {
        final StoragePath path = StoragePath.parse("/label/Label111");

        this.loadAndCheck(
            this.createStorage(),
            path,
            this.createContext(),
            StorageValue.with(
                path,
                Optional.of(MAPPING1)
            ).setContentType(
                SpreadsheetMediaTypes.MEMORY_LABEL
            )
        );
    }

    @Test
    public void testLoadWithLabel2WrongSpreadsheet() {
        final StoragePath path = StoragePath.parse("/label/Label222");

        this.loadAndCheck(
            this.createStorage(),
            path,
            this.createContext()
        );
    }

    @Test
    public void testLoadWithUnknownLabel() {
        this.loadAndCheck(
            this.createStorage(),
            StoragePath.parse("/label/UnknownLabel404"),
            this.createContext()
        );
    }

    @Test
    public void testLoadWithSpreadsheetAndCell() {
        final StoragePath path = StoragePath.parse("/spreadsheet/111/cell/A1");

        this.loadAndCheck(
            this.createStorage(),
            path,
            this.createContext(),
            StorageValue.with(
                path,
                Optional.of(
                    CELL1.setFormattedValue(
                        Optional.of(
                            TextNode.text("111.")
                        )
                    )
                )
            ).setContentType(
                SpreadsheetMediaTypes.MEMORY_CELL
            )
        );
    }

    @Test
    public void testLoadWithSpreadsheetAndCell2() {
        final StoragePath path = StoragePath.parse("/spreadsheet/222/cell/B2");

        this.loadAndCheck(
            this.createStorage(),
            path,
            this.createContext(),
            StorageValue.with(
                path,
                Optional.of(
                    CELL2.setFormattedValue(
                        Optional.of(
                            TextNode.text("222.")
                        )
                    )
                )
            ).setContentType(
                SpreadsheetMediaTypes.MEMORY_CELL
            )
        );
    }

    @Test
    public void testLoadWithSpreadsheetAndLabel() {
        final StoragePath path = StoragePath.parse("/spreadsheet/111/label/Label111");

        this.loadAndCheck(
            this.createStorage(),
            path,
            this.createContext(),
            StorageValue.with(
                path,
                Optional.of(MAPPING1)
            ).setContentType(
                SpreadsheetMediaTypes.MEMORY_LABEL
            )
        );
    }

    @Test
    public void testLoadWithSpreadsheetAndLabel2() {
        final StoragePath path = StoragePath.parse("/spreadsheet/222/label/Label222");

        this.loadAndCheck(
            this.createStorage(),
            path,
            this.createContext(),
            StorageValue.with(
                path,
                Optional.of(MAPPING2)
            ).setContentType(
                SpreadsheetMediaTypes.MEMORY_LABEL
            )
        );
    }

    // Storage.save.....................................................................................................

    @Test
    public void testSaveWithSpreadsheetId() {
        final TestSpreadsheetTerminalStorageContext context = this.createContext();

        final SpreadsheetId spreadsheetId = SpreadsheetId.with(0x333);

        final SpreadsheetMetadata metadata = METADATA1.set(
            SpreadsheetMetadataPropertyName.SPREADSHEET_ID,
            spreadsheetId
        ).set(
            SpreadsheetMetadataPropertyName.SPREADSHEET_NAME,
            SpreadsheetName.with("Spreadsheet333")
        );

        this.saveAndCheck(
            this.createStorage(),
            StorageValue.with(
                StoragePath.parse("/spreadsheet"),
                Optional.of(metadata)
            ),
            context,
            StorageValue.with(
                StoragePath.parse("/spreadsheet/333"),
                Optional.of(
                    metadata
                )
            ).setContentType(SpreadsheetMediaTypes.MEMORY_SPREADSHEET_METADATA)
        );

        this.checkEquals(
            Optional.of(metadata),
            context.loadMetadata(spreadsheetId)
        );
    }

    @Test
    public void testSaveWithCell() {
        final TestSpreadsheetTerminalStorageContext context = this.createContext();

        final StoragePath path = StoragePath.parse("/cell");

        this.saveAndCheck(
            this.createStorage(),
            StorageValue.with(
                path,
                Optional.of(DIFFERENT_UNFORMATTED_CELL)
            ),
            context,
            StorageValue.with(
                path,
                Optional.of(DIFFERENT_FORMATTED_CELL)
            ).setContentType(SpreadsheetMediaTypes.MEMORY_CELL)
        );

        this.checkEquals(
            Optional.of(DIFFERENT_FORMATTED_CELL),
            context.spreadsheetIdSpreadsheetStoreRepository.apply(SPREADSHEET_ID1)
                .cells()
                .load(DIFFERENT_CELL_REFERENCE)
        );
    }

    @Test
    public void testSaveWithLabel() {
        final TestSpreadsheetTerminalStorageContext context = this.createContext();

        final StoragePath path = StoragePath.parse("/label/DifferentLabel");

        this.saveAndCheck(
            this.createStorage(),
            StorageValue.with(
                path,
                Optional.of(DIFFERENT_MAPPING)
            ),
            context,
            StorageValue.with(
                path,
                Optional.of(DIFFERENT_MAPPING)
            ).setContentType(SpreadsheetMediaTypes.MEMORY_LABEL)
        );

        this.checkEquals(
            Optional.of(DIFFERENT_MAPPING),
            context.spreadsheetIdSpreadsheetStoreRepository.apply(SPREADSHEET_ID1)
                .labels()
                .load(DIFFERENT_MAPPING.label())
        );
    }

    @Test
    public void testSaveWithSpreadsheetIdAndCell() {
        final TestSpreadsheetTerminalStorageContext context = this.createContext();

        final StoragePath path = StoragePath.parse("/spreadsheet/111/cell");

        this.saveAndCheck(
            this.createStorage(),
            StorageValue.with(
                path,
                Optional.of(DIFFERENT_UNFORMATTED_CELL)
            ),
            context,
            StorageValue.with(
                path,
                Optional.of(DIFFERENT_FORMATTED_CELL)
            ).setContentType(SpreadsheetMediaTypes.MEMORY_CELL)
        );

        this.checkEquals(
            Optional.of(DIFFERENT_FORMATTED_CELL),
            context.spreadsheetIdSpreadsheetStoreRepository.apply(SPREADSHEET_ID1)
                .cells()
                .load(DIFFERENT_CELL_REFERENCE)
        );
    }

    @Test
    public void testSaveWithSpreadsheetIdAndCell2() {
        final TestSpreadsheetTerminalStorageContext context = this.createContext();

        final StoragePath path = StoragePath.parse("/spreadsheet/222/cell");

        this.saveAndCheck(
            this.createStorage(),
            StorageValue.with(
                path,
                Optional.of(DIFFERENT_UNFORMATTED_CELL)
            ),
            context,
            StorageValue.with(
                path,
                Optional.of(DIFFERENT_FORMATTED_CELL)
            ).setContentType(SpreadsheetMediaTypes.MEMORY_CELL)
        );

        this.checkEquals(
            Optional.of(DIFFERENT_FORMATTED_CELL),
            context.spreadsheetIdSpreadsheetStoreRepository.apply(SPREADSHEET_ID2)
                .cells()
                .load(DIFFERENT_CELL_REFERENCE)
        );
    }

    @Test
    public void testSaveWithSpreadsheetIdLabel1() {
        final TestSpreadsheetTerminalStorageContext context = this.createContext();

        final StoragePath path = StoragePath.parse("/spreadsheet/111/label/DifferentLabel");

        this.saveAndCheck(
            this.createStorage(),
            StorageValue.with(
                path,
                Optional.of(DIFFERENT_MAPPING)
            ),
            context,
            StorageValue.with(
                path,
                Optional.of(DIFFERENT_MAPPING)
            ).setContentType(SpreadsheetMediaTypes.MEMORY_LABEL)
        );

        this.checkEquals(
            Optional.of(DIFFERENT_MAPPING),
            context.setSpreadsheetId(SPREADSHEET_ID1)
                .spreadsheetEngineContext()
                .storeRepository()
                .labels()
                .load(DIFFERENT_MAPPING.label())
        );
    }

    @Test
    public void testSaveWithSpreadsheetIdLabel2() {
        final TestSpreadsheetTerminalStorageContext context = this.createContext();

        final StoragePath path = StoragePath.parse("/spreadsheet/222/label/DifferentLabel");

        this.saveAndCheck(
            this.createStorage(),
            StorageValue.with(
                path,
                Optional.of(DIFFERENT_MAPPING)
            ),
            context,
            StorageValue.with(
                path,
                Optional.of(DIFFERENT_MAPPING)
            ).setContentType(SpreadsheetMediaTypes.MEMORY_LABEL)
        );

        this.checkEquals(
            Optional.of(DIFFERENT_MAPPING),
            context.setSpreadsheetId(SPREADSHEET_ID2)
                .spreadsheetEngineContext()
                .storeRepository()
                .labels()
                .load(DIFFERENT_MAPPING.label())
        );
    }

    // Storage.delete...................................................................................................

    @Test
    public void testDeleteWithUnknownSpreadsheetId() {
        this.createStorage()
            .delete(
                StoragePath.parse("/spreadsheet/404"),
                this.createContext()
            );
    }

    @Test
    public void testDeleteWithSpreadsheetId() {
        final SpreadsheetTerminalStorageSpreadsheet storage = this.createStorage();
        final TestSpreadsheetTerminalStorageContext context = this.createContext();

        // must delete spreadsheet/222 because context is for spreadsheet/111
        final StoragePath path = StoragePath.parse("/spreadsheet/222");

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
    public void testDeleteWithUnknownCell() {
        this.createStorage()
            .delete(
                StoragePath.parse("/spreadsheet/111/cell/Z9"),
                this.createContext()
            );
    }

    @Test
    public void testDeleteWithCell() {
        final SpreadsheetTerminalStorageSpreadsheet storage = this.createStorage();
        final TestSpreadsheetTerminalStorageContext context = this.createContext();

        final StoragePath path = StoragePath.parse("/cell/A1");

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
    public void testDeleteWithLabel() {
        final SpreadsheetTerminalStorageSpreadsheet storage = this.createStorage();
        final TestSpreadsheetTerminalStorageContext context = this.createContext();

        final StoragePath path = StoragePath.parse("/label/Label111");

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
    public void testDeleteWithSpreadsheetIdAndCell() {
        final SpreadsheetTerminalStorageSpreadsheet storage = this.createStorage();
        final TestSpreadsheetTerminalStorageContext context = this.createContext();

        storage.delete(
            StoragePath.parse("/spreadsheet/111/cell/A1"),
            context
        );

        this.loadAndCheck(
            storage,
            StoragePath.parse("/cell/A1"),
            context
        );
    }

    @Test
    public void testDeleteWithSpreadsheetIdAndCell2() {
        final SpreadsheetTerminalStorageSpreadsheet storage = this.createStorage();
        final TestSpreadsheetTerminalStorageContext context = this.createContext();

        final StoragePath path = StoragePath.parse("/spreadsheet/222/cell/B2");

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
    public void testDeleteWithSpreadsheetIdAndLabel() {
        final SpreadsheetTerminalStorageSpreadsheet storage = this.createStorage();
        final TestSpreadsheetTerminalStorageContext context = this.createContext();

        final StoragePath path = StoragePath.parse("/spreadsheet/111/label/Label111");

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
    public void testDeleteWithSpreadsheetIdAndLabel2() {
        final SpreadsheetTerminalStorageSpreadsheet storage = this.createStorage();
        final TestSpreadsheetTerminalStorageContext context = this.createContext();

        final StoragePath path = StoragePath.parse("/spreadsheet/222/label/Label222");

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

    // Storage.list.....................................................................................................

    @Test
    public void testListWithSpreadsheet() {
        final SpreadsheetTerminalStorageSpreadsheet storage = this.createStorage();
        final TestSpreadsheetTerminalStorageContext context = this.createContext();

        this.listAndCheck(
            storage,
            StoragePath.parse("/spreadsheet"),
            0, // offset
            3, // count
            context,
            METADATA_INFO1,
            METADATA_INFO2
        );
    }

    @Test
    public void testListWithSpreadsheetAndOffset() {
        final SpreadsheetTerminalStorageSpreadsheet storage = this.createStorage();
        final TestSpreadsheetTerminalStorageContext context = this.createContext();

        this.listAndCheck(
            storage,
            StoragePath.parse("/spreadsheet"),
            1, // offset
            3, // count
            context,
            METADATA_INFO2
        );
    }

    @Test
    public void testListWithSpreadsheetAndOffset2() {
        final SpreadsheetTerminalStorageSpreadsheet storage = this.createStorage();
        final TestSpreadsheetTerminalStorageContext context = this.createContext();

        this.listAndCheck(
            storage,
            StoragePath.parse("/spreadsheet"),
            2, // offset
            3, // count
            context
        );
    }

    @Test
    public void testListWithSpreadsheetAndSize() {
        final SpreadsheetTerminalStorageSpreadsheet storage = this.createStorage();
        final TestSpreadsheetTerminalStorageContext context = this.createContext();

        this.listAndCheck(
            storage,
            StoragePath.parse("/spreadsheet"),
            0, // offset
            1, // count
            context,
            METADATA_INFO1
        );
    }

    @Test
    public void testListWithCell() {
        final SpreadsheetTerminalStorageSpreadsheet storage = this.createStorage();
        final TestSpreadsheetTerminalStorageContext context = this.createContext();

        this.listAndCheck(
            storage,
            StoragePath.parse("/cell"),
            0, // offset
            2, // count
            context,
            StorageValueInfo.with(
                StoragePath.parse("/cell/A1"),
                AUDIT_INFO
            )
        );
    }

    @Test
    public void testListWithLabel() {
        final SpreadsheetTerminalStorageSpreadsheet storage = this.createStorage();
        final TestSpreadsheetTerminalStorageContext context = this.createContext();

        this.listAndCheck(
            storage,
            StoragePath.parse("/label"),
            0, // offset
            2, // count
            context,
            StorageValueInfo.with(
                StoragePath.parse("/label/Label111"),
                AUDIT_INFO
            )
        );
    }

    @Test
    public void testListWithSpreadsheetIdCell1() {
        final SpreadsheetTerminalStorageSpreadsheet storage = this.createStorage();
        final TestSpreadsheetTerminalStorageContext context = this.createContext();

        this.listAndCheck(
            storage,
            StoragePath.parse("/spreadsheet/111/cell"),
            0, // offset
            2, // count
            context,
            StorageValueInfo.with(
                StoragePath.parse("/spreadsheet/111/cell/A1"),
                AUDIT_INFO
            )
        );
    }

    @Test
    public void testListWithSpreadsheetIdCell2() {
        final SpreadsheetTerminalStorageSpreadsheet storage = this.createStorage();
        final TestSpreadsheetTerminalStorageContext context = this.createContext();

        this.listAndCheck(
            storage,
            StoragePath.parse("/spreadsheet/222/cell"),
            0, // offset
            2, // count
            context,
            StorageValueInfo.with(
                StoragePath.parse("/spreadsheet/222/cell/B2"),
                AUDIT_INFO
            )
        );
    }

    @Test
    public void testListWithSpreadsheetIdLabel1() {
        final SpreadsheetTerminalStorageSpreadsheet storage = this.createStorage();
        final TestSpreadsheetTerminalStorageContext context = this.createContext();

        this.listAndCheck(
            storage,
            StoragePath.parse("/spreadsheet/111/label/"),
            0, // offset
            2, // count
            context,
            StorageValueInfo.with(
                StoragePath.parse("/spreadsheet/111/label/Label111"),
                AUDIT_INFO
            )
        );
    }

    @Test
    public void testListWithSpreadsheetIdLabel2() {
        final SpreadsheetTerminalStorageSpreadsheet storage = this.createStorage();
        final TestSpreadsheetTerminalStorageContext context = this.createContext();

        this.listAndCheck(
            storage,
            StoragePath.parse("/spreadsheet/222/label/"),
            0, // offset
            2, // count
            context,
            StorageValueInfo.with(
                StoragePath.parse("/spreadsheet/222/label/Label222"),
                AUDIT_INFO
            )
        );
    }

    @Override
    public SpreadsheetTerminalStorageSpreadsheet createStorage() {
        final SpreadsheetEngine engine = SpreadsheetEngines.basic();

        return SpreadsheetTerminalStorageSpreadsheet.with(
            SpreadsheetTerminalStorages.cell(engine),
            SpreadsheetTerminalStorages.label(engine),
            SpreadsheetTerminalStorages.metadata()
        );
    }

    @Override
    public TestSpreadsheetTerminalStorageContext createContext() {
        final SpreadsheetMetadataStore metadataStore = SpreadsheetMetadataStores.treeMap();
        metadataStore.save(METADATA1);
        metadataStore.save(METADATA2);

        final Map<SpreadsheetId, SpreadsheetStoreRepository> spreadsheetIdSpreadsheetStoreRepository = Maps.sorted();

        {
            final SpreadsheetStoreRepository repo1 = SpreadsheetStoreRepositories.treeMap(metadataStore);

            repo1.cells()
                .save(CELL1);
            repo1.labels()
                .save(MAPPING1);

            spreadsheetIdSpreadsheetStoreRepository.put(
                SPREADSHEET_ID1,
                repo1
            );
        }

        {
            final SpreadsheetStoreRepository repo2 = SpreadsheetStoreRepositories.treeMap(metadataStore);

            repo2.cells()
                .save(CELL2);
            repo2.labels()
                .save(MAPPING2);

            spreadsheetIdSpreadsheetStoreRepository.put(
                SPREADSHEET_ID2,
                repo2
            );
        }

        return new TestSpreadsheetTerminalStorageContext(
            SPREADSHEET_ID1,
            (final SpreadsheetId id) -> {
                final SpreadsheetStoreRepository repo = spreadsheetIdSpreadsheetStoreRepository.get(id);
                if (null == repo) {
                    throw new IllegalArgumentException("SpreadsheetStoreRepository: Missing for SpreadsheetId " + id);
                }
                return repo;
            }
        );
    }

    final static class TestSpreadsheetTerminalStorageContext extends FakeStorageContext implements SpreadsheetTerminalStorageContext,
        SpreadsheetEngineContextDelegator,
        TerminalContextDelegator {

        TestSpreadsheetTerminalStorageContext(final SpreadsheetId spreadsheetId,
                                              final Function<SpreadsheetId, SpreadsheetStoreRepository> spreadsheetIdSpreadsheetStoreRepository) {
            this.spreadsheetId = spreadsheetId;
            this.spreadsheetIdSpreadsheetStoreRepository = spreadsheetIdSpreadsheetStoreRepository;

            this.spreadsheetEngineContext = SpreadsheetEngineContexts.basic(
                SpreadsheetMetadataMode.SCRIPTING,
                SpreadsheetContexts.basic(
                    spreadsheetIdSpreadsheetStoreRepository,
                    SPREADSHEET_PROVIDER,
                    (c) -> SpreadsheetEngineContexts.basic(
                        SpreadsheetMetadataMode.FORMULA,
                        c,
                        TERMINAL_CONTEXT
                    ),
                    (SpreadsheetEngineContext c) ->
                        new Router<>() {
                            @Override
                            public Optional<HttpHandler> route(final Map<HttpRequestAttribute<?>, Object> parameters) {
                                throw new UnsupportedOperationException();
                            }
                        },
                    SpreadsheetEnvironmentContexts.basic(
                        EnvironmentContexts.map(ENVIRONMENT_CONTEXT)
                            .setEnvironmentValue(
                                SpreadsheetEnvironmentContext.SERVER_URL,
                                Url.parseAbsolute("https://example.com")
                            ).setEnvironmentValue(
                                SpreadsheetEnvironmentContext.SPREADSHEET_ID,
                                spreadsheetId
                            ).setUser(
                                Optional.of(
                                    EmailAddress.parse("user@example.com")
                                )
                            )
                    ),
                    LOCALE_CONTEXT,
                    PROVIDER_CONTEXT,
                    TERMINAL_SERVER_CONTEXT
                ),
                SpreadsheetMetadataTesting.TERMINAL_CONTEXT
            );
        }

        @Override
        public SpreadsheetId spreadsheetId() {
            return this.spreadsheetId;
        }

        private final SpreadsheetId spreadsheetId;

        @Override
        public TestSpreadsheetTerminalStorageContext setSpreadsheetId(final SpreadsheetId spreadsheetId) {
            Objects.requireNonNull(spreadsheetId, "spreadsheetId");

            return this.spreadsheetId.equals(spreadsheetId) ?
                this :
                new TestSpreadsheetTerminalStorageContext(
                    spreadsheetId,
                    this.spreadsheetIdSpreadsheetStoreRepository
                );
        }

        private final Function<SpreadsheetId, SpreadsheetStoreRepository> spreadsheetIdSpreadsheetStoreRepository;

        @Override
        public SpreadsheetEngineContext spreadsheetEngineContext() {
            return this.spreadsheetEngineContext;
        }

        private final SpreadsheetEngineContext spreadsheetEngineContext;

        @Override
        public SpreadsheetTerminalStorageSpreadsheetMetadataTest.TestSpreadsheetTerminalStorageContext cloneEnvironment() {
            throw new UnsupportedOperationException();
        }

        @Override
        public SpreadsheetTerminalStorageContext setEnvironmentContext(final EnvironmentContext environmentContext) {
            Objects.requireNonNull(environmentContext, "environmentContext");
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> SpreadsheetTerminalStorageSpreadsheetMetadataTest.TestSpreadsheetTerminalStorageContext setEnvironmentValue(final EnvironmentValueName<T> name,
                                                                                                                               final T value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public SpreadsheetTerminalStorageSpreadsheetMetadataTest.TestSpreadsheetTerminalStorageContext removeEnvironmentValue(final EnvironmentValueName<?> name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public SpreadsheetTerminalStorageSpreadsheetMetadataTest.TestSpreadsheetTerminalStorageContext setLineEnding(final LineEnding lineEnding) {
            throw new UnsupportedOperationException();
        }

        @Override
        public SpreadsheetTerminalStorageSpreadsheetMetadataTest.TestSpreadsheetTerminalStorageContext setLocale(final Locale locale) {
            throw new UnsupportedOperationException();
        }

        @Override
        public LocalDateTime now() {
            return HAS_NOW.now();
        }

        @Override
        public Optional<EmailAddress> user() {
            return this.spreadsheetEngineContext()
                .user();
        }

        @Override
        public SpreadsheetTerminalStorageSpreadsheetMetadataTest.TestSpreadsheetTerminalStorageContext setUser(final Optional<EmailAddress> user) {
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
        public SpreadsheetTerminalStorageSpreadsheetMetadataTest.TestSpreadsheetTerminalStorageContext exitTerminal() {
            throw new UnsupportedOperationException();
        }
    }

    // toString.........................................................................................................

    @Test
    public void testToString() {
        this.toStringAndCheck(
            SpreadsheetTerminalStorageSpreadsheet.with(
                CELLS,
                LABELS,
                METADATAS
            ),
            "/cell " + CELLS + ", /label " + LABELS + ", /spreadsheet " + METADATAS
        );
    }

    // class............................................................................................................

    @Override
    public Class<SpreadsheetTerminalStorageSpreadsheet> type() {
        return SpreadsheetTerminalStorageSpreadsheet.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }
}
