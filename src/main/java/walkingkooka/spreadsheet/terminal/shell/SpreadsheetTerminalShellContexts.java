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
import walkingkooka.reflect.PublicStaticHelper;
import walkingkooka.terminal.TerminalContext;
import walkingkooka.terminal.shell.TerminalShellContext;

import java.util.Optional;
import java.util.function.Function;

/**
 * A collection of {@link walkingkooka.terminal.server.TerminalServerContext}
 */
public final class SpreadsheetTerminalShellContexts implements PublicStaticHelper {

    /**
     * {@see SpreadsheetTerminalShellContext}
     */
    public static TerminalShellContext spreadsheet(final Function<String, Optional<Object>> evaluator,
                                                   final CanConvert canConvert,
                                                   final TerminalContext terminalContext) {
        return SpreadsheetTerminalShellContext.with(
            evaluator,
            canConvert,
            terminalContext
        );
    }

    /**
     * Stop creation
     */
    private SpreadsheetTerminalShellContexts() {
        throw new UnsupportedOperationException();
    }
}
