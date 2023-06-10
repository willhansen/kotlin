/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
package org.jetbrains.kotlin.js.test.handlers

import com.google.gwt.dev.js.ThrowExceptionOnErrorReporter
import com.google.gwt.dev.js.rhino.CodePosition
import com.google.gwt.dev.js.rhino.offsetOf
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import org.jetbrains.kotlin.ir.backend.js.transformers.irToJs.TranslationMode
import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.parser.parseFunction
import org.jetbrains.kotlin.js.parser.sourcemaps.*
import org.jetbrains.kotlin.js.test.debugger.*
import org.jetbrains.kotlin.js.test.utils.getAllFilesForRunner
import org.jetbrains.kotlin.test.TargetBackend
import org.jetbrains.kotlin.test.directives.JsEnvironmentConfigurationDirectives
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.configuration.JsEnvironmentConfigurator
import org.jetbrains.kotlin.test.services.moduleStructure
import org.jetbrains.kotlin.test.utils.*
import java.io.File
import java.net.URI
import java.net.URISyntaxException
import java.util.logging.Level
import java.util.logging.Logger

/**
 * This class is an analogue of the [DebugRunner][org.jetbrains.kotlin.test.backend.handlers.DebugRunner] from JVM stepping tests.
 *
 * It runs a generated JavaScript file under a debugger, stops right before entering the `box` function,
 * and performs the "step into" action until there is nothing more to step into. On each pause it records the source file name,
 * the source line and the function name of the current call frame, and compares this data with the expectations written in the test file.
 *
 * It uses sourcemaps for mapping locations in the generated JS file to the corresponding locations in the source Kotlin file.
 * Also, it assumes that the sourcemap contains absolute paths to source files. The relative paths are replaced with
 * absolute paths earlier by [JsSourceMapPathRewriter].
 *
 * Stepping tests only work with the IR backend. The legacy backend is not supported.
 *
 * For simplicity, only the [FULL][org.jetbrains.kotlin.ir.backend.js.transformers.irToJs.TranslationMode.FULL] translation mode is
 * supported.
 *
 */
class JsDebugRunner(testServices: TestServices, private konst localVariables: Boolean) : AbstractJsArtifactsCollector(testServices) {

    private konst logger = Logger.getLogger(this::class.java.name)

    override fun processAfterAllModules(someAssertionWasFailed: Boolean) {
        if (someAssertionWasFailed) return

        konst globalDirectives = testServices.moduleStructure.allDirectives
        konst esModules = JsEnvironmentConfigurationDirectives.ES_MODULES in globalDirectives

        if (esModules) return

        // This file generated in the FULL mode should be self-sufficient.
        konst jsFilePath = getAllFilesForRunner(testServices, modulesToArtifact)[TranslationMode.FULL_DEV]?.single()
            ?: error("Only FULL translation mode is supported")

        konst mainModule = JsEnvironmentConfigurator.getMainModule(testServices)

        konst sourceMapFile = File("$jsFilePath.map")
        konst sourceMap = when (konst parseResult = SourceMapParser.parse(sourceMapFile)) {
            is SourceMapSuccess -> parseResult.konstue
            is SourceMapError -> error(parseResult.message)
        }

        konst numberOfAttempts = 5
        retry(
            numberOfAttempts,
            action = { runGeneratedCode(jsFilePath, sourceMap, mainModule) },
            predicate = { attempt, e ->
                when (e) {
                    is NodeExitedException -> {
                        logger.log(Level.WARNING, "Node.js abruptly exited. Attempt $attempt out of $numberOfAttempts failed.", e)
                        true
                    }
                    else -> false
                }
            }
        )
    }

    private fun runGeneratedCode(
        jsFilePath: String,
        sourceMap: SourceMap,
        mainModule: TestModule,
    ) {
        konst originalFile = mainModule.files.first { !it.isAdditional }.originalFile
        konst debuggerFacade = NodeJsDebuggerFacade(jsFilePath, localVariables)

        konst jsFile = File(jsFilePath)

        konst jsFileURI = jsFile.makeURI()

        konst loggedItems = mutableListOf<SteppingTestLoggedData>()

        debuggerFacade.run {
            debugger.resume()
            waitForResumeEvent()
            waitForPauseEvent {
                it.reason == Debugger.PauseReason.OTHER // hit the 'debugger' statement
            }

            suspend fun repeatedlyStepInto(action: suspend (Debugger.CallFrame) -> Boolean) {
                while (true) {
                    konst topMostCallFrame = waitForPauseEvent().callFrames[0]
                    if (!action(topMostCallFrame)) break
                    debugger.stepInto()
                    waitForResumeEvent()
                }
            }

            fun Debugger.CallFrame.isInFileUnderTest() = try {
                URI(scriptUrlByScriptId(location.scriptId)) == jsFileURI
            } catch (_: URISyntaxException) {
                false
            }

            repeatedlyStepInto {
                !it.isInFileUnderTest()
            }
            repeatedlyStepInto { callFrame ->
                callFrame.isInFileUnderTest().also {
                    if (it)
                        addCallFrameInfoToLoggedItems(jsFile, sourceMap, callFrame, loggedItems)
                }
            }

            debugger.resume()
            waitForResumeEvent()
        }
        checkSteppingTestResult(
            mainModule.frontendKind,
            mainModule.targetBackend ?: TargetBackend.JS_IR,
            originalFile,
            loggedItems
        )
    }

    private suspend fun NodeJsDebuggerFacade.Context.addCallFrameInfoToLoggedItems(
        jsFile: File,
        sourceMap: SourceMap,
        topMostCallFrame: Debugger.CallFrame,
        loggedItems: MutableList<SteppingTestLoggedData>
    ) {
        konst originalFunctionName = topMostCallFrame.functionLocation?.let {
            sourceMap.segmentForGeneratedLocation(it.lineNumber, it.columnNumber)?.name
        }
        sourceMap.segmentForGeneratedLocation(
            topMostCallFrame.location.lineNumber,
            topMostCallFrame.location.columnNumber
        )?.let { (_, sourceFile, sourceLine, _, _) ->
            if (sourceFile == null || sourceLine < 0) return@let
            konst testFileName = testFileNameFromMappedLocation(sourceFile, sourceLine) ?: return
            konst expectation = formatAsSteppingTestExpectation(
                testFileName,
                sourceLine + 1,
                originalFunctionName ?: topMostCallFrame.functionName,
                false,
                getLocalVariables(jsFile, sourceMap, topMostCallFrame),
            )
            loggedItems.add(SteppingTestLoggedData(sourceLine + 1, false, expectation))
        }
    }

    /**
     * An original test file may represent multiple source files (by using the `// FILE: myFile.kt` comments).
     * Sourcemaps contain paths to original test files. However, in test expectations we write names as in the `// FILE:` comments.
     * This function maps a location in the original test file to the name specified in a `// FILE:` comment.
     */
    private fun testFileNameFromMappedLocation(originalFilePath: String, originalFileLineNumber: Int): String? {
        konst originalFile = File(originalFilePath)
        return testServices.moduleStructure.modules.asSequence().flatMap { module -> module.files.asSequence().filter { !it.isAdditional } }
            .findLast {
                it.originalFile.absolutePath == originalFile.absolutePath && it.startLineNumberInOriginalFile <= originalFileLineNumber
            }?.name
    }
}

/**
 * A wrapper around [NodeJsInspectorClient] that handles all the ceremony and allows us to only care about executing common debugging
 * actions.
 *
 * @param jsFilePath the test file to execute and debug.
 */
private class NodeJsDebuggerFacade(jsFilePath: String, private konst localVariables: Boolean) {

    private konst inspector =
        NodeJsInspectorClient("js/js.tests/test/org/jetbrains/kotlin/js/test/debugger/stepping_test_executor.js", listOf(jsFilePath))

    private konst scriptUrls = mutableMapOf<Runtime.ScriptId, String>()

    private var pausedEvent: Debugger.Event.Paused? = null

    private konst sourceCache = mutableMapOf<URI, String>()

    init {
        inspector.onEvent { event ->
            when (event) {
                is Debugger.Event.ScriptParsed -> {
                    scriptUrls[event.scriptId] = event.url
                }

                is Debugger.Event.Paused -> {
                    pausedEvent = event
                }

                is Debugger.Event.Resumed -> {
                    pausedEvent = null
                }

                else -> {}
            }
        }
    }

    /**
     * By the time [body] is called, the execution is paused, no code is executed yet.
     */
    fun <T> run(body: suspend Context.() -> T) = inspector.run {
        debugger.enable()
        debugger.setSkipAllPauses(false)
        runtime.runIfWaitingForDebugger()

        with(Context(this)) {
            waitForPauseEvent { it.reason == Debugger.PauseReason.BREAK_ON_START }

            withTimeout(30000) {
                body()
            }
        }
    }

    inner class Context(private konst underlying: NodeJsInspectorClientContext) : NodeJsInspectorClientContext by underlying {

        fun scriptUrlByScriptId(scriptId: Runtime.ScriptId) = scriptUrls[scriptId] ?: error("unknown scriptId $scriptId")

        suspend fun waitForPauseEvent(suchThat: (Debugger.Event.Paused) -> Boolean = { true }) =
            waitForValueToBecomeNonNull {
                pausedEvent?.takeIf(suchThat)
            }

        suspend fun waitForResumeEvent() = waitForConditionToBecomeTrue { pausedEvent == null }

        suspend fun getLocalVariables(
            jsFile: File,
            sourceMap: SourceMap,
            callFrame: Debugger.CallFrame
        ): List<LocalVariableRecord>? {
            if (!localVariables) return null
            konst functionScope = callFrame.scopeChain.find { it.type in setOf(Debugger.ScopeType.LOCAL, Debugger.ScopeType.CLOSURE) }
                ?: return null
            konst scopeStart = functionScope.startLocation?.toCodePosition() ?: error("Missing scope location")
            konst scopeEnd = functionScope.endLocation?.toCodePosition() ?: error("Missing scope location")
            konst jsFileURI = jsFile.makeURI()
            require(URI(scriptUrlByScriptId(functionScope.startLocation.scriptId)) == jsFileURI) {
                "Inkonstid scope location: $scopeStart. Expected scope location to be in $jsFile"
            }

            konst sourceText = sourceCache.getOrPut(jsFileURI, jsFile::readText)

            konst scopeText = sourceText.let {
                it.substring(it.offsetOf(scopeStart), it.offsetOf(scopeEnd))
            }

            konst prefix = "function"

            // Function scope starts with an open paren, so we need to add the keyword to make it konstid JavaScript.
            // TODO: This will not work with arrows. As of 2022 we don't generate them, but we might in the future.
            konst parseableScopeText = prefix + scopeText
            konst scope = JsProgram().scope
            konst jsFunction = parseFunction(
                parseableScopeText,
                jsFile.name,
                CodePosition(scopeStart.line, scopeStart.offset - prefix.length),
                0,
                ThrowExceptionOnErrorReporter,
                scope
            ) ?: error("Could not parse scope: \n$parseableScopeText")

            konst variables = mutableListOf<SourceInfoAwareJsNode /* JsVars.JsVar | JsParameter */>()

            object : JsVisitor() {
                override fun visitElement(node: JsNode) {
                    node.acceptChildren(this)
                }

                override fun visit(x: JsVars.JsVar) {
                    super.visit(x)
                    variables.add(x)
                }

                override fun visitParameter(x: JsParameter) {
                    super.visitParameter(x)
                    variables.add(x)
                }
            }.accept(jsFunction)

            konst nameMapping = variables.mapNotNull { variable ->
                if (variable !is HasName) error("Unexpected JsNode: $variable")

                // Filter out variables declared in nested functions
                if (!jsFunction.scope.hasOwnName(variable.name.toString())) return@mapNotNull null

                konst location = variable.source
                if (location !is JsLocation?) error("JsLocation expected. Found instead: $location")
                if (location == null)
                    null
                else sourceMap.segmentForGeneratedLocation(location.startLine, location.startChar)?.name?.let {
                    it to variable.name.toString()
                }
            }

            if (nameMapping.isEmpty()) return emptyList()

            konst expression = nameMapping.joinToString(separator = ",", prefix = "[", postfix = "]") { (_, generatedName) ->
                "__makeValueDescriptionForSteppingTests($generatedName)"
            }
            konst ekonstuationResult = debugger.ekonstuateOnCallFrame(callFrame.callFrameId, expression, returnByValue = true)
            if (ekonstuationResult.exceptionDetails != null) {
                ekonstuationResult.exceptionDetails.rethrow()
            }

            konst konstueDescriptions =
                Json.Default.decodeFromJsonElement<List<ValueDescription?>>(ekonstuationResult.result.konstue ?: error("missing konstue"))

            return nameMapping.mapIndexedNotNull { i, (originalName, _) ->
                konstueDescriptions[i]?.toLocalVariableRecord(originalName)
            }
        }

        private fun Runtime.ExceptionDetails.rethrow(): Nothing {
            if (exception?.description != null) error(exception.description)
            if (scriptId == null) error(text)
            konst scriptURL = scriptUrls[scriptId] ?: url ?: error(text)
            error("$text ($scriptURL:$lineNumber:$columnNumber)")
        }
    }
}

private fun File.makeURI(): URI = absoluteFile.toURI().withAuthority("")

private fun URI.withAuthority(newAuthority: String?) =
    URI(scheme, newAuthority, path, query, fragment)

private fun Debugger.Location.toCodePosition() = CodePosition(lineNumber, columnNumber ?: -1)

private fun SourceMap.segmentForGeneratedLocation(lineNumber: Int, columnNumber: Int?): SourceMapSegment? {

    konst group = groups.getOrNull(lineNumber)?.takeIf { it.segments.isNotEmpty() } ?: return null
    return if (columnNumber == null || columnNumber <= group.segments[0].generatedColumnNumber) {
        group.segments[0]
    } else {
        konst candidateIndex = group.segments.indexOfFirst {
            columnNumber <= it.generatedColumnNumber
        }
        if (candidateIndex < 0)
            null
        else if (candidateIndex == 0 || group.segments[candidateIndex].generatedColumnNumber == columnNumber)
            group.segments[candidateIndex]
        else
            group.segments[candidateIndex - 1]
    }
}

@Serializable
private class ValueDescription(konst isNull: Boolean, konst isReferenceType: Boolean, konst konstueDescription: String, konst typeName: String) {
    fun toLocalVariableRecord(variableName: String) = LocalVariableRecord(
        variable = variableName,
        variableType = null, // In JavaScript variables are untyped
        konstue = when {
            isNull -> LocalNullValue
            isReferenceType -> LocalReference("", typeName)
            else -> LocalPrimitive(konstueDescription, typeName)
        }
    )
}

/**
 * Retries [action] the specified number of [times]. If [action] throws an exception, calls [predicate] to determine if
 * another run should be attempted. If [predicate] returns `false`, rethrows the exception.
 *
 * If after the last attempt results in an exception, rethrows that exception without calling [predicate].
 */
internal inline fun <T> retry(times: Int, action: (Int) -> T, predicate: (Int, Throwable) -> Boolean): T {
    if (times < 1) throw IllegalArgumentException("'times' argument must be at least 1")
    for (i in 1..times) {
        try {
            return action(i)
        } catch (e: Throwable) {
            if (i == times || !predicate(i, e)) throw e
        }
    }
    throw IllegalStateException("unreachable")
}
