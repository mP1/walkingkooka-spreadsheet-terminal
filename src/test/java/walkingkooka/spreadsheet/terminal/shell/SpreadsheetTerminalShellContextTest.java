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

import org.junit.jupiter.api.Test;
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.set.Sets;
import walkingkooka.environment.EnvironmentContexts;
import walkingkooka.io.FakeTextReader;
import walkingkooka.io.TextReader;
import walkingkooka.net.Url;
import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.reflect.ClassTesting2;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.route.Router;
import walkingkooka.spreadsheet.SpreadsheetContexts;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContexts;
import walkingkooka.spreadsheet.engine.SpreadsheetMetadataMode;
import walkingkooka.spreadsheet.environment.SpreadsheetEnvironmentContext;
import walkingkooka.spreadsheet.environment.SpreadsheetEnvironmentContexts;
import walkingkooka.spreadsheet.expression.SpreadsheetExpressionEvaluationContext;
import walkingkooka.spreadsheet.expression.SpreadsheetExpressionFunctions;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStore;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStores;
import walkingkooka.spreadsheet.provider.SpreadsheetProviders;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReferenceLoaders;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepositories;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.terminal.FakeTerminalContext;
import walkingkooka.terminal.TerminalContexts;
import walkingkooka.terminal.shell.TerminalShellContextTesting;
import walkingkooka.terminal.shell.TerminalShells;
import walkingkooka.text.cursor.TextCursors;
import walkingkooka.text.printer.FakePrinter;
import walkingkooka.text.printer.Printer;
import walkingkooka.tree.expression.ExpressionFunctionName;
import walkingkooka.tree.expression.function.ExpressionFunctionParameter;
import walkingkooka.tree.expression.function.FakeExpressionFunction;
import walkingkooka.tree.expression.function.provider.ExpressionFunctionAliasSet;
import walkingkooka.tree.expression.function.provider.ExpressionFunctionProviders;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public final class SpreadsheetTerminalShellContextTest implements TerminalShellContextTesting<SpreadsheetTerminalShellContext>,
    SpreadsheetMetadataTesting,
    ClassTesting2<SpreadsheetTerminalShellContext> {

    @Override
    public void testEvaluateWithNullTextFails() {
        throw new UnsupportedOperationException();
    }

    @Test
    public void testStartInvalidExpression() {
        this.startAndCheck(
            Lists.of("1+2+"),
            Lists.empty(),
            Lists.of("#ERROR")
        );
    }

    @Test
    public void testStartMathExpression() {
        this.startAndCheck(
            Lists.of("1+2"),
            Lists.of("3")
        );
    }

    @Test
    public void testStartMathExpression2() {
        this.startAndCheck(
            Lists.of(
                "1+2",
                "3+4"
            ),
            Lists.of(
                "3",
                "7"
            )
        );
    }

    @Test
    public void testStartStringLiteral() {
        this.startAndCheck(
            Lists.of("\"Hello\""),
            Lists.of("Hello")
        );
    }

    @Test
    public void testStartFunction() {
        this.startAndCheck(
            Lists.of("hello()"),
            Lists.of("HelloWorld123")
        );
    }

    private void startAndCheck(final List<String> input,
                               final List<String> expectedOutput) {
        this.startAndCheck(
            input,
            expectedOutput,
            Lists.empty() // error
        );
    }

    private void startAndCheck(final List<String> input,
                               final List<String> expectedOutput,
                               final List<String> expectedError) {
        final List<String> output = Lists.array();
        final List<String> error = Lists.array();

        TerminalShells.basic(100)
            .start(
                this.createContext(
                    input,
                    output::add,
                    error::add
                )
            );

        this.checkEquals(
            expectedOutput,
            output
        );
        this.checkEquals(
            expectedError,
            error
        );
    }

    @Override
    public SpreadsheetTerminalShellContext createContext() {
        return this.createContext(
            Lists.empty(),
            new StringBuilder()::append,
            new StringBuilder()::append
        );
    }

    private SpreadsheetTerminalShellContext createContext(final Iterable<String> lines,
                                                          final Consumer<String> output,
                                                          final Consumer<String> error) {
        final Iterator<String> linesIterator = lines.iterator();

        final SpreadsheetMetadataStore store = SpreadsheetMetadataStores.treeMap();

        final SpreadsheetId spreadsheetId = SpreadsheetId.with(1);

        final ExpressionFunctionAliasSet functions = SpreadsheetExpressionFunctions.parseAliasSet("hello");

        final SpreadsheetMetadata metadata = store.save(
            METADATA_EN_AU.set(
                SpreadsheetMetadataPropertyName.FUNCTIONS,
                functions
            ).set(
                SpreadsheetMetadataPropertyName.SCRIPTING_FUNCTIONS,
                functions
            ).set(
                SpreadsheetMetadataPropertyName.SPREADSHEET_ID,
                spreadsheetId
            )
        );

        final SpreadsheetStoreRepository repo = SpreadsheetStoreRepositories.treeMap(store);

        final SpreadsheetEngineContext[] engineContexts = new SpreadsheetEngineContext[1];

        engineContexts[0] = SpreadsheetEngineContexts.basic(
            SpreadsheetMetadataMode.SCRIPTING,
            SpreadsheetContexts.basic(
                (idid) -> repo,
                metadata.spreadsheetProvider(
                    SpreadsheetProviders.basic(
                        CONVERTER_PROVIDER,
                        //EXPRESSION_FUNCTION_PROVIDER,
                        ExpressionFunctionProviders.basic(
                            Url.parseAbsolute("https://example.com/functions"),
                            SpreadsheetExpressionFunctions.NAME_CASE_SENSITIVITY,
                            Sets.of(
                                new FakeExpressionFunction<>() {

                                    @Override
                                    public Optional<ExpressionFunctionName> name() {
                                        return Optional.of(
                                            ExpressionFunctionName.with("hello")
                                        );
                                    }

                                    @Override
                                    public List<ExpressionFunctionParameter<?>> parameters(final int count) {
                                        return NO_PARAMETERS;
                                    }

                                    @Override
                                    public Object apply(final List<Object> values,
                                                        final SpreadsheetExpressionEvaluationContext context) {
                                        return "HelloWorld123";
                                    }
                                }
                            )
                        ),
                        SPREADSHEET_COMPARATOR_PROVIDER,
                        SPREADSHEET_EXPORTER_PROVIDER,
                        SPREADSHEET_FORMATTER_PROVIDER,
                        FORM_HANDLER_PROVIDER,
                        SPREADSHEET_IMPORTER_PROVIDER,
                        SPREADSHEET_PARSER_PROVIDER,
                        VALIDATOR_PROVIDER
                    )
                ),
                (c) -> SpreadsheetEngineContexts.basic(
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
                            spreadsheetId
                        )
                ),
                LOCALE_CONTEXT,
                PROVIDER_CONTEXT,
                TERMINAL_SERVER_CONTEXT
            ),
            TerminalContexts.fake()
        );

        final SpreadsheetEngineContext engineContext = engineContexts[0];

        final SpreadsheetExpressionEvaluationContext expressionEvaluationContext = engineContext.spreadsheetExpressionEvaluationContext(
            SpreadsheetExpressionEvaluationContext.NO_CELL,
            SpreadsheetExpressionReferenceLoaders.empty()
        );

        return SpreadsheetTerminalShellContext.with(
            (s) -> Optional.ofNullable(
                expressionEvaluationContext.evaluateExpression(
                    expressionEvaluationContext.parseFormula(
                            TextCursors.charSequence(s)
                        ).toExpression(expressionEvaluationContext)
                        .orElseThrow(() -> new IllegalStateException("Invalid expression"))
                )
            ),
            engineContext, // canConvert
            new FakeTerminalContext() {

                @Override
                public boolean isTerminalOpen() {
                    return linesIterator.hasNext();
                }

                @Override
                public TextReader input() {
                    return new FakeTextReader() {

                        @Override
                        public Optional<String> readLine(final long timeout) {
                            return Optional.ofNullable(
                                linesIterator.hasNext() ?
                                    linesIterator.next() :
                                    null
                            );
                        }
                    };
                }

                @Override
                public Printer output() {
                    return new FakePrinter() {
                        @Override
                        public void println(final CharSequence chars) {
                            output.accept(
                                chars.toString()
                            );
                        }
                    };
                }

                @Override
                public Printer error() {
                    return new FakePrinter() {
                        @Override
                        public void println(final CharSequence chars) {
                            error.accept(
                                chars.toString()
                            );
                        }
                    };
                }
            }
        );
    }

    // class............................................................................................................

    @Override
    public Class<SpreadsheetTerminalShellContext> type() {
        return SpreadsheetTerminalShellContext.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }
}
