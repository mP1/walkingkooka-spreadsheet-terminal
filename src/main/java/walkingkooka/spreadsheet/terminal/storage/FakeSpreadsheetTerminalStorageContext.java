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

import walkingkooka.environment.EnvironmentValueName;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngineContext;
import walkingkooka.text.LineEnding;

import java.util.Locale;
import java.util.Optional;

public class FakeSpreadsheetTerminalStorageContext extends FakeSpreadsheetEngineContext implements SpreadsheetTerminalStorageContext {

    public FakeSpreadsheetTerminalStorageContext() {
        super();
    }

    @Override
    public boolean isTerminalInteractive() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<String> readLine(final long timeout) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void print(final CharSequence text) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LineEnding lineEnding() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void flush() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException();
    }

    // EnvironmentContext...............................................................................................

    @Override
    public SpreadsheetTerminalStorageContext cloneEnvironment() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Optional<T> environmentValue(final EnvironmentValueName<T> name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> SpreadsheetTerminalStorageContext setEnvironmentValue(final EnvironmentValueName<T> name,
                                                                     final T value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpreadsheetTerminalStorageContext removeEnvironmentValue(final EnvironmentValueName<?> name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpreadsheetTerminalStorageContext setLocale(final Locale locale) {
        throw new UnsupportedOperationException();
    }
}
