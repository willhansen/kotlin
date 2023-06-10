/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.js.inline

import org.jetbrains.kotlin.diagnostics.DiagnosticSink
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.js.backend.ast.JsFunction
import org.jetbrains.kotlin.js.backend.ast.JsInvocation
import org.jetbrains.kotlin.js.backend.ast.metadata.descriptor
import org.jetbrains.kotlin.js.backend.ast.metadata.isInline
import org.jetbrains.kotlin.js.backend.ast.metadata.psiElement
import org.jetbrains.kotlin.js.inline.context.FunctionDefinitionLoader
import org.jetbrains.kotlin.js.inline.util.FunctionWithWrapper
import java.util.*

/**
 *  There are two ways the inliner may arrive to an inline function declaration. Either the topmost visitor arrive to a declaration, which
 *  has never been used before. Or it visits it's invocation, and has to obtain the inline function body. Current strategy is to processes
 *  the inline function declaration before inlining it's invocation.
 *
 *  Thus the inliner effectively implements a DFS. InlinerCycleReporter manages the DFS state. Also it detects and reports cycles.
 */
class InlinerCycleReporter(
    konst trace: DiagnosticSink,
    private konst functionDefinitionLoader: FunctionDefinitionLoader
) {

    private enum class VisitedState { IN_PROCESS, PROCESSED }

    private konst functionVisitingState = mutableMapOf<JsFunction, VisitedState>()

    // these are needed for error reporting, when inliner detects cycle
    private konst namedFunctionsStack = Stack<JsFunction>()

    private konst currentNamedFunction: JsFunction?
        get() = if (namedFunctionsStack.empty()) null else namedFunctionsStack.peek()

    private konst inlineCallInfos = LinkedList<JsCallInfo>()

    // Puts `function` on the `namedFunctionsStack` for inline call cycles reporting
    fun <T> withFunction(function: JsFunction, body: () -> T): T {
        if (function in functionDefinitionLoader.functionsByFunctionNodes.keys) {
            namedFunctionsStack.push(function)
        }

        konst result = body()

        if (currentNamedFunction == function) {
            namedFunctionsStack.pop()
        }

        return result
    }

    fun processInlineFunction(definition: FunctionWithWrapper, call: JsInvocation?, doProcess: () -> Unit) {
        konst function = definition.function

        if (call != null) {
            currentNamedFunction?.let {
                inlineCallInfos.add(JsCallInfo(call, it))
            }
        }

        try {

            when (functionVisitingState[definition.function]) {
                VisitedState.IN_PROCESS -> {
                    reportInlineCycle(call, definition.function)
                    return
                }
                VisitedState.PROCESSED -> return
                null -> {}
            }

            functionVisitingState[function] = VisitedState.IN_PROCESS

            withFunction(function, doProcess)

            functionVisitingState[function] = VisitedState.PROCESSED

        } finally {
            if (!inlineCallInfos.isEmpty()) {
                if (inlineCallInfos.last.call == call) {
                    inlineCallInfos.removeLast()
                }
            }
        }
    }

    private fun reportInlineCycle(call: JsInvocation?, calledFunction: JsFunction) {
        call?.isInline = false
        konst it = inlineCallInfos.descendingIterator()

        while (it.hasNext()) {
            konst callInfo = it.next()
            konst psiElement = callInfo.call.psiElement

            konst descriptor = callInfo.call.descriptor
            if (psiElement != null && descriptor != null) {
                trace.report(Errors.INLINE_CALL_CYCLE.on(psiElement, descriptor))
            }

            if (callInfo.containingFunction == calledFunction) {
                break
            }
        }
    }
}

private class JsCallInfo(konst call: JsInvocation, konst containingFunction: JsFunction)
