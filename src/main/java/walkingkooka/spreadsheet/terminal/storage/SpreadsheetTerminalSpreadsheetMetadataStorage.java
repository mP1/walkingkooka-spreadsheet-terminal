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

import walkingkooka.collect.list.ImmutableList;
import walkingkooka.net.header.MediaType;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.SpreadsheetMediaTypes;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStore;
import walkingkooka.storage.Storage;
import walkingkooka.storage.StorageName;
import walkingkooka.storage.StoragePath;
import walkingkooka.storage.StorageValue;
import walkingkooka.storage.StorageValueInfo;

import java.util.List;
import java.util.Optional;

/**
 * A {@link Storage} that supports CRUD operations for {@link SpreadsheetMetadata}.
 */
final class SpreadsheetTerminalSpreadsheetMetadataStorage extends SpreadsheetTerminalStorage {

    /**
     * Singleton
     */
    final static SpreadsheetTerminalSpreadsheetMetadataStorage INSTANCE = new SpreadsheetTerminalSpreadsheetMetadataStorage();

    private final static MediaType MEDIA_TYPE = SpreadsheetMediaTypes.MEMORY_SPREADSHEET_METADATA;

    private SpreadsheetTerminalSpreadsheetMetadataStorage() {
        super();
    }

    /**
     * Loads the {@link SpreadsheetMetadata} with the given {@link SpreadsheetId}.
     * <pre>
     * /spreadsheet/SpreadsheetId
     * </pre>
     */
    @Override
    Optional<StorageValue> loadNonNull(final StoragePath path,
                                       final SpreadsheetTerminalStorageContext context) {
        final List<StorageName> storageNames = path.namesList();

        switch (storageNames.size()) {
            case 2:
                return context.storeRepository()
                    .metadatas()
                    .load(
                        context.convertOrFail(
                            storageNames.get(1)
                                .value(),
                            SpreadsheetId.class
                        )
                    )
                    .map(m -> StorageValue.with(
                            path,
                            Optional.of(m)
                        ).setContentType(MEDIA_TYPE)
                    );
            default:
                throw new IllegalArgumentException("Invalid path");
        }
    }

    @Override
    StorageValue saveNonNull(final StorageValue value,
                             final SpreadsheetTerminalStorageContext context) {
        final StoragePath path = value.path();
        final List<StorageName> storageNames = path.namesList();

        switch (storageNames.size()) {
            case 1:
                break;
            case 2:
                throw new IllegalArgumentException("Invalid path, SpreadsheetId not accepted");
            default:
                throw new IllegalArgumentException("Invalid path");
        }

        final SpreadsheetMetadata saved = context.storeRepository()
            .metadatas()
            .save(
                context.convertOrFail(
                    value.value()
                        .orElseThrow(() -> new IllegalArgumentException("Missing " + SpreadsheetMetadata.class.getSimpleName())),
                    SpreadsheetMetadata.class
                )
            );

        return value.setPath(
            StoragePath.ROOT.append(
                StorageName.with(
                    saved.getOrFail(SpreadsheetMetadataPropertyName.SPREADSHEET_ID).toString()
                )
            )
        ).setValue(
            Optional.ofNullable(saved)
        ).setContentType(MEDIA_TYPE);
    }

    /**
     * Deletes the {@link SpreadsheetId} in the path.
     */
    @Override
    void deleteNonNull(final StoragePath path,
                       final SpreadsheetTerminalStorageContext context) {
        final List<StorageName> names = path.namesList();
        switch (names.size()) {
            case 0:
            case 1:
                throw new IllegalArgumentException("Missing " + SpreadsheetId.class.getSimpleName());
            case 2:
                final SpreadsheetMetadataStore store = context.storeRepository()
                    .metadatas();
                store.delete(
                    context.convertOrFail(
                        names.get(1)
                            .value(),
                        SpreadsheetId.class
                    )
                );
                break;
            default:
                throw new IllegalArgumentException("Invalid path");
        }
    }

    /**
     * Supports listing using the provided {@link walkingkooka.spreadsheet.SpreadsheetName} pattern.
     */
    @Override
    List<StorageValueInfo> listNonNull(final StoragePath path,
                                       final int offset,
                                       final int count,
                                       final SpreadsheetTerminalStorageContext context) {
        final List<StorageName> names = path.namesList();

        final String name;

        switch (names.size()) {
            case 2:
                name = context.convertOrFail(
                    names.get(1)
                        .value(),
                    String.class
                );
                break;
            default:
                throw new IllegalArgumentException("Invalid path");
        }

        return context.storeRepository()
            .metadatas()
            .findByName(
                name,
                offset,
                count
            ).stream()
            .map(m ->
                StorageValueInfo.with(
                    StoragePath.ROOT.append(
                        StorageName.with(
                            m.getOrFail(SpreadsheetMetadataPropertyName.SPREADSHEET_ID)
                                .toString()
                        )
                    ),
                    context.createdAuditInfo()
                )
            ).collect(ImmutableList.collector());
    }

    // Object...........................................................................................................

    @Override
    public String toString() {
        return SpreadsheetMetadataStore.class.getSimpleName();
    }
}
