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

package walkingkooka.spreadsheet.terminal.shell;

import walkingkooka.convert.CanConvert;
import walkingkooka.spreadsheet.SpreadsheetErrorKind;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.terminal.TerminalContext;
import walkingkooka.terminal.TerminalContextDelegator;
import walkingkooka.terminal.shell.TerminalShellContext;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * A {@link TerminalShellContext} that delegates all methods to another {@link TerminalContext}, but uses the given
 * {@link SpreadsheetEngineContext} to parse and evaluate any given {@link String} command.
 */
final class SpreadsheetTerminalShellContext implements TerminalShellContext,
    TerminalContextDelegator {

    static SpreadsheetTerminalShellContext with(final Function<String, Optional<Object>> evaluator,
                                                final CanConvert canConvert,
                                                final TerminalContext terminalContext) {
        return new SpreadsheetTerminalShellContext(
            Objects.requireNonNull(evaluator, "evaluator"),
            Objects.requireNonNull(canConvert, "canConvert"),
            Objects.requireNonNull(terminalContext, "terminalContext")
        );
    }

    private SpreadsheetTerminalShellContext(final Function<String, Optional<Object>> evaluator,
                                            final CanConvert canConvert,
                                            final TerminalContext terminalContext) {
        super();

        this.evaluator = evaluator;
        this.canConvert = canConvert;
        this.terminalContext = terminalContext;
    }

    // TerminalShellContext.............................................................................................

    @Override
    public void evaluate(final String command) {
        Optional<Object> value;

        try {
            value = this.evaluator.apply(command);
        } catch (final UnsupportedOperationException rethrow) {
            throw rethrow;
        } catch (final RuntimeException cause) {
            cause.printStackTrace();

            value = Optional.of(
                SpreadsheetErrorKind.translate(cause)
            );
        }

        final Object valueOrNull = value.orElse(null);

        this.terminalContext.println(
            this.canConvert.convert(
                valueOrNull,
                String.class
            ).orElseLeftGet(() -> String.valueOf(valueOrNull))
        );
    }

    /**
     * Accepts the given expression, parse it, evaluates the expression and returns any value.
     */
    private final Function<String, Optional<Object>> evaluator;

    /**
     * Used to convert the value returned by the {@link #evaluator}.
     */
    private final CanConvert canConvert;

    // TerminalContextDelegator.........................................................................................

    @Override
    public TerminalContext terminalContext() {
        return this.terminalContext;
    }

    private final TerminalContext terminalContext;

    // String...........................................................................................................

    @Override
    public String toString() {
        return this.getClass().getSimpleName() +
            "evaluator=" + this.evaluator +
            ", canConvert=" + this.canConvert +
            ", terminalContext=" + this.terminalContext;
    }
}
