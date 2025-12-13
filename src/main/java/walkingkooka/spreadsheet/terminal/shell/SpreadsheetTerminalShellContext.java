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
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.value.SpreadsheetErrorKind;
import walkingkooka.terminal.TerminalContext;
import walkingkooka.terminal.TerminalContextDelegator;
import walkingkooka.terminal.expression.TerminalExpressionEvaluationContext;
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

        final TerminalContext terminalContext = this.terminalContext;
        final CanConvert canConvert = this.canConvert;

        try {
            terminalContext.output()
                .println(
                    canConvert.convertOrFail(
                        this.evaluator.apply(command),
                        String.class
                    )
                );
        } catch (final UnsupportedOperationException rethrow) {
            throw rethrow;
        } catch (final RuntimeException cause) {
            terminalContext.error()
                .println(
                    canConvert.convertOrFail(
                        SpreadsheetErrorKind.translate(cause),
                        String.class
                    )
                );
        }
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

    @Override
    public TerminalExpressionEvaluationContext terminalExpressionEvaluationContext() {
        throw new UnsupportedOperationException();
    }

    // String...........................................................................................................

    @Override
    public String toString() {
        return this.getClass().getSimpleName() +
            "evaluator=" + this.evaluator +
            ", canConvert=" + this.canConvert +
            ", terminalContext=" + this.terminalContext;
    }
}
