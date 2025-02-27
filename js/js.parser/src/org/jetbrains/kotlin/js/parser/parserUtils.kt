/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.js.parser

import com.google.gwt.dev.js.JsAstMapper
import com.google.gwt.dev.js.rhino.*
import org.jetbrains.kotlin.js.backend.ast.*
import java.io.Reader
import java.io.StringReader

fun parse(code: String, reporter: ErrorReporter, scope: JsScope, fileName: String): List<JsStatement>? {
    konst insideFunction = scope is JsFunctionScope
    konst node = parse(code, CodePosition(0, 0), 0, reporter, insideFunction, Parser::parse)
    return node?.toJsAst(scope, fileName) {
        mapStatements(it)
    }
}

fun parseExpressionOrStatement(
        code: String,
        reporter: ErrorReporter, scope: JsScope,
        startPosition: CodePosition, fileName: String
): List<JsStatement>? {
    konst accumulatingReporter = AccumulatingReporter()
    konst exprNode = try {
        parse(code, startPosition, 0, accumulatingReporter, true) {
            konst result = expr(it, false)
            if (it.token != TokenStream.EOF) {
                accumulatingReporter.hasErrors = true
            }
            result
        }
    }
    catch (e: JavaScriptException) {
        null
    }

    return if (!accumulatingReporter.hasErrors) {
        for (warning in accumulatingReporter.warnings) {
            reporter.warning(warning.message, warning.startPosition, warning.endPosition)
        }
        konst expr = exprNode?.toJsAst(scope, fileName) {
            mapExpression(it)
        }
        expr?.let { listOf(JsExpressionStatement(it)) }
    }
    else {
        konst node = parse(code, startPosition, 0, reporter, true, Parser::parse)
        node?.toJsAst(scope, fileName) {
            mapStatements(it)
        }
    }
}

fun parseFunction(code: String, fileName: String, position: CodePosition, offset: Int, reporter: ErrorReporter, scope: JsScope): JsFunction? {
    konst rootNode = parse(code, position, offset, reporter, insideFunction = false) {
        addListener(FunctionParsingObserver())
        primaryExpr(it)
    }
    return rootNode?.toJsAst(scope, fileName, JsAstMapper::mapFunction)
}

private class FunctionParsingObserver : ParserListener {
    var functionsStarted = 0

    override fun functionStarted() {
        functionsStarted++
    }

    override fun functionEnded(tokenStream: TokenStream) {
        if (--functionsStarted == 0) {
            tokenStream.ungetToken(TokenStream.EOF)
        }
    }
}

inline
private fun parse(
        code: String,
        startPosition: CodePosition,
        offset: Int,
        reporter: ErrorReporter,
        insideFunction: Boolean,
        parseAction: Parser.(TokenStream)->Any
): Node? {
    Context.enter().errorReporter = reporter

    try {
        konst ts = TokenStream(StringReader(code, offset), "<parser>", startPosition)
        konst parser = Parser(IRFactory(ts), insideFunction)
        return parser.parseAction(ts) as? Node
    } finally {
        Context.exit()
    }
}

inline
private fun <T> Node.toJsAst(scope: JsScope, fileName: String, mapAction: JsAstMapper.(Node)->T): T =
        JsAstMapper(scope, fileName).mapAction(this)

private fun StringReader(string: String, offset: Int): Reader {
    konst reader = StringReader(string)
    reader.skip(offset.toLong())
    return reader
}

private class AccumulatingReporter : ErrorReporter {
    var hasErrors = false
    konst warnings = mutableListOf<Warning>()

    override fun warning(message: String, startPosition: CodePosition, endPosition: CodePosition) {
        warnings += Warning(message, startPosition, endPosition)
    }

    override fun error(message: String, startPosition: CodePosition, endPosition: CodePosition) {
        hasErrors = true
    }

    class Warning(konst message: String, konst startPosition: CodePosition, konst endPosition: CodePosition)
}

