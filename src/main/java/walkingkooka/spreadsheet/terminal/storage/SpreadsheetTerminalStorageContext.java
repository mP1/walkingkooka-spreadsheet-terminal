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
import walkingkooka.net.email.EmailAddress;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.storage.StorageContext;
import walkingkooka.terminal.TerminalContext;
import walkingkooka.text.LineEnding;

import java.util.Locale;
import java.util.Optional;

public interface SpreadsheetTerminalStorageContext extends TerminalContext, StorageContext, SpreadsheetEngineContext {

    @Override
    SpreadsheetTerminalStorageContext setSpreadsheetId(final SpreadsheetId spreadsheetId);

    @Override
    SpreadsheetTerminalStorageContext cloneEnvironment();

    @Override
    SpreadsheetTerminalStorageContext setEnvironmentContext(final EnvironmentContext environmentContext);

    @Override
    <T> SpreadsheetTerminalStorageContext setEnvironmentValue(final EnvironmentValueName<T> name,
                                                              final T reference);

    @Override
    SpreadsheetTerminalStorageContext removeEnvironmentValue(final EnvironmentValueName<?> name);

    @Override
    SpreadsheetTerminalStorageContext setLineEnding(final LineEnding lineEnding);

    @Override
    SpreadsheetTerminalStorageContext setLocale(final Locale locale);

    @Override
    SpreadsheetTerminalStorageContext setUser(final Optional<EmailAddress> user);

    @Override
    SpreadsheetTerminalStorageContext exitTerminal();
}
