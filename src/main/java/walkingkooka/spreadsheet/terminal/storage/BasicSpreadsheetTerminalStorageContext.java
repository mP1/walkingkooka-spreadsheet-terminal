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
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContextDelegator;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.terminal.TerminalContext;
import walkingkooka.terminal.TerminalContextDelegator;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

final class BasicSpreadsheetTerminalStorageContext implements SpreadsheetTerminalStorageContext,
    SpreadsheetEngineContextDelegator,
    TerminalContextDelegator {

    static BasicSpreadsheetTerminalStorageContext with(final SpreadsheetEngineContext spreadsheetEngineContext,
                                                       final TerminalContext terminalContext) {
        return new BasicSpreadsheetTerminalStorageContext(
            Objects.requireNonNull(spreadsheetEngineContext, "spreadsheetEngineContext"),
            Objects.requireNonNull(terminalContext, "terminalContext")
        );
    }

    private BasicSpreadsheetTerminalStorageContext(final SpreadsheetEngineContext spreadsheetEngineContext,
                                                   final TerminalContext terminalContext) {
        this.spreadsheetEngineContext = spreadsheetEngineContext;
        this.terminalContext = terminalContext;
    }

    @Override
    public Optional<SpreadsheetSelection> resolveLabel(final SpreadsheetLabelName labelName) {
        return this.spreadsheetEngineContext.resolveLabel(labelName);
    }

    // EnvironmentContextDelegator......................................................................................

    @Override
    public EnvironmentContext environmentContext() {
        return this.spreadsheetEngineContext;
    }

    @Override
    public SpreadsheetTerminalStorageContext cloneEnvironment() {
        final SpreadsheetEngineContext spreadsheetEngineContext = this.spreadsheetEngineContext;
        final SpreadsheetEngineContext cloned = spreadsheetEngineContext.cloneEnvironment();

        return spreadsheetEngineContext.equals(cloned) ?
            this :
            new BasicSpreadsheetTerminalStorageContext(
                cloned,
                this.terminalContext
            );
    }

    @Override
    public <T> SpreadsheetTerminalStorageContext setEnvironmentValue(final EnvironmentValueName<T> name,
                                                                     final T reference) {
        this.spreadsheetEngineContext.setEnvironmentValue(
            name,
            reference
        );
        return this;
    }

    @Override
    public SpreadsheetTerminalStorageContext removeEnvironmentValue(final EnvironmentValueName<?> name) {
        this.spreadsheetEngineContext.removeEnvironmentValue(name);
        return this;
    }

    @Override
    public SpreadsheetTerminalStorageContext setLocale(final Locale locale) {
        this.spreadsheetEngineContext.setLocale(locale);
        return this;
    }

    @Override
    public SpreadsheetTerminalStorageContext setUser(final Optional<EmailAddress> user) {
        this.spreadsheetEngineContext.setUser(user);
        return this;
    }

    // SpreadsheetEngineContextDelegator................................................................................

    @Override
    public SpreadsheetEngineContext spreadsheetEngineContext() {
        return this.spreadsheetEngineContext;
    }

    private final SpreadsheetEngineContext spreadsheetEngineContext;

    // TerminalContextDelegator.........................................................................................

    @Override
    public SpreadsheetTerminalStorageContext quitTerminal() {
        this.terminalContext.quitTerminal();
        return this;
    }

    @Override
    public TerminalContext terminalContext() {
        return this.terminalContext;
    }

    private final TerminalContext terminalContext;

    // toString.........................................................................................................

    @Override
    public String toString() {
        return "spreadsheetEngineContext: " + spreadsheetEngineContext + "terminalContext: " + this.terminalContext;
    }
}
