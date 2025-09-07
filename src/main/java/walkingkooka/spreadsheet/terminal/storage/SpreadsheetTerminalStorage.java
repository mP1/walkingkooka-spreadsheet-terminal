/*
 * Copyright 2019 Miroslav Pokorny (github.com/mP1)
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

import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.storage.Storage;
import walkingkooka.storage.StoragePath;
import walkingkooka.storage.StorageValue;
import walkingkooka.storage.StorageValueInfo;
import walkingkooka.store.Store;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

abstract class SpreadsheetTerminalStorage implements Storage<SpreadsheetTerminalStorageContext> {

    SpreadsheetTerminalStorage(final SpreadsheetEngine engine) {
        super();
        this.engine = Objects.requireNonNull(engine, "engine");
    }

    @Override
    public final Optional<StorageValue> load(final StoragePath path,
                                             final SpreadsheetTerminalStorageContext context) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(context, "context");

        return this.load0(
            path,
            context
        );
    }

    abstract Optional<StorageValue> load0(final StoragePath path,
                                          final SpreadsheetTerminalStorageContext context);

    @Override
    public final StorageValue save(final StorageValue value,
                                   final SpreadsheetTerminalStorageContext context) {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(context, "context");

        return this.save0(
            value,
            context
        );
    }

    abstract StorageValue save0(final StorageValue value,
                                final SpreadsheetTerminalStorageContext context);

    @Override
    public final void delete(final StoragePath path,
                             final SpreadsheetTerminalStorageContext context) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(context, "context");

        this.delete0(
            path,
            context
        );
    }

    abstract void delete0(final StoragePath path,
                          final SpreadsheetTerminalStorageContext context);

    @Override
    public final List<StorageValueInfo> list(final StoragePath path,
                                             final int offset,
                                             final int count,
                                             final SpreadsheetTerminalStorageContext context) {
        Objects.requireNonNull(path, "path");
        Store.checkOffsetAndCount(offset, count);
        Objects.requireNonNull(context, "context");

        return this.list0(path, offset, count, context);
    }

    abstract List<StorageValueInfo> list0(final StoragePath path,
                                          final int offset,
                                          final int count,
                                          final SpreadsheetTerminalStorageContext context);

    final SpreadsheetEngine engine;
}
