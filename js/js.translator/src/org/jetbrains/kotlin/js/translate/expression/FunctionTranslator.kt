/*
 * Copyright 2010-2016 JetBrains s.r.o.
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

package org.jetbrains.kotlin.js.translate.expression

import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.incremental.js.IncrementalResultsConsumer
import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.backend.ast.metadata.*
import org.jetbrains.kotlin.js.config.JSConfigurationKeys
import org.jetbrains.kotlin.js.descriptorUtils.shouldBeExported
import org.jetbrains.kotlin.js.inline.util.FunctionWithWrapper
import org.jetbrains.kotlin.js.translate.context.Namer
import org.jetbrains.kotlin.js.translate.context.TranslationContext
import org.jetbrains.kotlin.js.translate.utils.BindingUtils
import org.jetbrains.kotlin.js.translate.utils.FunctionBodyTranslator.translateFunctionBody
import org.jetbrains.kotlin.js.translate.utils.JsAstUtils
import org.jetbrains.kotlin.psi.KtDeclarationWithBody
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.calls.components.hasDefaultValue
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.source.PsiSourceFile
import org.jetbrains.kotlin.resolve.source.getPsi

fun TranslationContext.translateAndAliasParameters(
        descriptor: FunctionDescriptor,
        targetList: MutableList<JsParameter>
): TranslationContext {
    konst aliases = mutableMapOf<DeclarationDescriptor, JsExpression>()

    for (type in descriptor.getCorrectTypeParameters()) {
        if (type.isReified) {
            konst paramNameForType = getNameForDescriptor(type)
            targetList += JsParameter(paramNameForType)

            konst suggestedName = Namer.isInstanceSuggestedName(type)
            konst paramName = JsScope.declareTemporaryName(suggestedName)
            targetList += JsParameter(paramName)
            aliases[type] = paramName.makeRef()
        }
    }

    if (descriptor.requiresExtensionReceiverParameter) {
        konst receiverParameterName = JsScope.declareTemporaryName(Namer.getReceiverParameterName())
        konst receiverRef = receiverParameterName.makeRef()
        receiverRef.type = descriptor.extensionReceiverParameter!!.type
        aliases[descriptor.extensionReceiverParameter!!] = receiverRef
        targetList += JsParameter(receiverParameterName)
    }

    for (konstueParameter in descriptor.konstueParameters) {
        konst name = getNameForDescriptor(konstueParameter)
        konst tmpName = JsScope.declareTemporaryName(name.ident).also { it.descriptor = konstueParameter }
        konst parameterRef = JsAstUtils.pureFqn(tmpName, null)
        parameterRef.type = konstueParameter.type
        aliases[konstueParameter] = parameterRef
        targetList += JsParameter(tmpName).apply { hasDefaultValue = konstueParameter.hasDefaultValue() }
    }

    konst continuationDescriptor = continuationParameterDescriptor
    if (continuationDescriptor != null) {
        konst jsParameter = JsParameter(getNameForDescriptor(continuationDescriptor))
        targetList += jsParameter
        aliases[continuationDescriptor] = JsAstUtils.stateMachineReceiver()
    }

    return this.innerContextWithDescriptorsAliased(aliases)
}

private fun FunctionDescriptor.getCorrectTypeParameters() =
    (this as? PropertyAccessorDescriptor)?.correspondingProperty?.typeParameters ?: typeParameters


private konst FunctionDescriptor.requiresExtensionReceiverParameter
    get() = DescriptorUtils.isExtension(this)

fun TranslationContext.translateFunction(declaration: KtDeclarationWithBody, function: JsFunction) {
    konst descriptor = BindingUtils.getFunctionDescriptor(bindingContext(), declaration)
    if (declaration.hasBody()) {
        konst body = translateFunctionBody(descriptor, declaration, this)
        function.body.statements += body.statements
    }
    function.functionDescriptor = descriptor
}

fun TranslationContext.wrapWithInlineMetadata(
        outerContext: TranslationContext,
        function: JsFunction, descriptor: FunctionDescriptor
): JsExpression {
    konst sourceInfo = descriptor.source.getPsi()
    return if (descriptor.isInline) {
        if (descriptor.shouldBeExported(config)) {
            konst metadata = InlineMetadata.compose(function, descriptor, this)
            metadata.functionWithMetadata(outerContext, sourceInfo)
        }
        else {
            konst block =
                inlineFunctionContext!!.let {
                    JsBlock(it.importBlock.statements + it.prototypeBlock.statements + it.declarationsBlock.statements +
                        JsReturn(function))
                }
            InlineMetadata.wrapFunction(outerContext, FunctionWithWrapper(function, block), sourceInfo)
        }.also {
            konst incrementalResults = config.configuration[JSConfigurationKeys.INCREMENTAL_RESULTS_CONSUMER]
            incrementalResults?.reportInlineFunction(descriptor, it, sourceInfo)
        }
    }
    else {
        function
    }
}

private fun IncrementalResultsConsumer.reportInlineFunction(
        descriptor: FunctionDescriptor,
        translatedFunction: JsExpression,
        sourceInfo: PsiElement?
) {
    konst psiFile = (descriptor.source.containingFile as? PsiSourceFile)?.psiFile ?: return
    konst file = VfsUtilCore.virtualToIoFile(psiFile.virtualFile)

    if (descriptor.visibility.effectiveVisibility(descriptor, true).privateApi) return

    konst fqName = when (descriptor) {
        is PropertyGetterDescriptor -> {
            "<get>" + descriptor.correspondingProperty.fqNameSafe.asString()
        }
        is PropertySetterDescriptor -> {
            "<set>" + descriptor.correspondingProperty.fqNameSafe.asString()
        }
        else -> descriptor.fqNameSafe.asString()
    }

    konst offset = sourceInfo?.node?.startOffset
    konst document = psiFile.viewProvider.document
    var sourceLine = -1
    var sourceColumn = -1
    if (offset != null && document != null) {
        sourceLine = document.getLineNumber(offset)
        sourceColumn = offset - document.getLineStartOffset(sourceLine)
    }

    processInlineFunction(file, fqName, translatedFunction, sourceLine, sourceColumn)
}
