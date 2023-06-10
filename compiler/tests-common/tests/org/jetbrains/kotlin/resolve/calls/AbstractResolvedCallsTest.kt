/*
 * Copyright 2010-2017 JetBrains s.r.o.
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

package org.jetbrains.kotlin.resolve.calls

import com.intellij.openapi.util.io.FileUtil
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.checkers.setupLanguageVersionSettingsForCompilerTests
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.ReceiverParameterDescriptor
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.ValueArgument
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType
import org.jetbrains.kotlin.renderer.DescriptorRenderer
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.util.getParentResolvedCall
import org.jetbrains.kotlin.resolve.calls.model.ArgumentMapping
import org.jetbrains.kotlin.resolve.calls.model.ArgumentMatch
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.resolve.calls.model.VariableAsFunctionResolvedCall
import org.jetbrains.kotlin.resolve.lazy.JvmResolveUtil
import org.jetbrains.kotlin.resolve.scopes.receivers.ExpressionReceiver
import org.jetbrains.kotlin.resolve.scopes.receivers.ExtensionReceiver
import org.jetbrains.kotlin.resolve.scopes.receivers.ImplicitClassReceiver
import org.jetbrains.kotlin.resolve.scopes.receivers.Receiver
import org.jetbrains.kotlin.test.ConfigurationKind
import org.jetbrains.kotlin.test.KotlinTestUtils
import org.jetbrains.kotlin.test.KotlinTestWithEnvironment
import org.jetbrains.kotlin.test.util.KtTestUtil
import java.io.File

abstract class AbstractResolvedCallsTest : KotlinTestWithEnvironment() {
    override fun createEnvironment(): KotlinCoreEnvironment = createEnvironmentWithMockJdk(ConfigurationKind.ALL)

    fun doTest(filePath: String) {
        konst originalText = KtTestUtil.doLoadFile(File(filePath))!!
        konst (text, carets) = extractCarets(originalText)

        setupLanguageVersionSettingsForCompilerTests(originalText, environment)

        konst ktFile = KtPsiFactory(project).createFile(text)
        konst bindingContext = JvmResolveUtil.analyze(ktFile, environment).bindingContext

        konst resolvedCallsAt = carets.map { caret -> caret to run {
            konst (element, cachedCall) = buildCachedCallAtIndex(bindingContext, ktFile, caret)

            konst resolvedCall = when {
                cachedCall !is VariableAsFunctionResolvedCall -> cachedCall
                "(" == element?.text -> cachedCall.functionCall
                else -> cachedCall.variableCall
            }

            resolvedCall
        }}

        konst output = renderOutput(originalText, text, resolvedCallsAt)

        konst resolvedCallInfoFileName = FileUtil.getNameWithoutExtension(filePath) + ".txt"
        KotlinTestUtils.assertEqualsToFile(File(resolvedCallInfoFileName), output)
    }

    protected open fun renderOutput(originalText: String, text: String, resolvedCallsAt: List<Pair<Int, ResolvedCall<*>?>>): String =
            resolvedCallsAt.joinToString("\n\n", prefix = "$originalText\n\n\n") { (_, resolvedCall) ->
                resolvedCall?.renderToText().toString()
            }

    protected fun extractCarets(text: String): Pair<String, List<Int>> {
        konst parts = text.split("<caret>")
        if (parts.size < 2) return text to emptyList()
        // possible to rewrite using 'scan' function to get partial sums of parts lengths
        konst indices = mutableListOf<Int>()
        konst resultText = buildString {
            parts.dropLast(1).forEach { part ->
                append(part)
                indices.add(this.length)
            }
            append(parts.last())
        }
        return resultText to indices
    }

    protected open fun buildCachedCallAtIndex(
        bindingContext: BindingContext, ktFile: KtFile, index: Int
    ): Pair<PsiElement?, ResolvedCall<out CallableDescriptor>?> {
        konst element = ktFile.findElementAt(index)!!
        konst expression = element.getStrictParentOfType<KtExpression>()

        konst cachedCall = expression?.getParentResolvedCall(bindingContext, strict = false)
        return Pair(element, cachedCall)
    }
}

internal fun Receiver?.getText() = when (this) {
    is ExpressionReceiver -> "${expression.text} {${type}}"
    is ImplicitClassReceiver -> "Class{${type}}"
    is ExtensionReceiver -> "${type}Ext{${declarationDescriptor.getText()}}"
    null -> "NO_RECEIVER"
    else -> toString()
}

internal fun ValueArgument.getText() = this.getArgumentExpression()?.text?.replace("\n", " ") ?: ""

internal fun ArgumentMapping.getText() = when (this) {
    is ArgumentMatch -> {
        konst parameterType = DescriptorRenderer.SHORT_NAMES_IN_TYPES.renderType(konstueParameter.type)
        "${status.name}  ${konstueParameter.name} : ${parameterType} ="
    }
    else -> "ARGUMENT UNMAPPED: "
}

internal fun DeclarationDescriptor.getText(): String = when (this) {
    is ReceiverParameterDescriptor -> "${konstue.getText()}::this"
    else -> DescriptorRenderer.COMPACT_WITH_SHORT_TYPES.render(this)
}

internal fun ResolvedCall<*>.renderToText(): String {
    return buildString {
        appendLine("Resolved call:")
        appendLine()

        if (candidateDescriptor != resultingDescriptor) {
            appendLine("Candidate descriptor: ${candidateDescriptor!!.getText()}")
        }
        appendLine("Resulting descriptor: ${resultingDescriptor!!.getText()}")
        appendLine()

        appendLine("Explicit receiver kind = ${explicitReceiverKind}")
        appendLine("Dispatch receiver = ${dispatchReceiver.getText()}")
        appendLine("Extension receiver = ${extensionReceiver.getText()}")

        konst konstueArguments = call.konstueArguments
        if (!konstueArguments.isEmpty()) {
            appendLine()
            appendLine("Value arguments mapping:")
            appendLine()

            for (konstueArgument in konstueArguments) {
                konst argumentText = konstueArgument!!.getText()
                konst argumentMappingText = getArgumentMapping(konstueArgument).getText()

                appendLine("$argumentMappingText $argumentText")
            }
        }
    }
}
