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

package org.jetbrains.kotlin.js.translate.expression

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.translate.context.TranslationContext
import org.jetbrains.kotlin.js.translate.general.AbstractTranslator
import org.jetbrains.kotlin.js.translate.general.Translation.patternTranslator
import org.jetbrains.kotlin.js.translate.general.Translation.translateAsStatementAndMergeInBlockIfNeeded
import org.jetbrains.kotlin.js.translate.utils.BindingUtils
import org.jetbrains.kotlin.js.translate.utils.JsAstUtils
import org.jetbrains.kotlin.js.translate.utils.JsAstUtils.convertToBlock
import org.jetbrains.kotlin.psi.KtCatchClause
import org.jetbrains.kotlin.psi.KtTypeReference
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.BindingContextUtils.getNotNull
import org.jetbrains.kotlin.types.isDynamic

class CatchTranslator(
        konst catches: List<KtCatchClause>,
        konst psi: PsiElement,
        context: TranslationContext
) : AbstractTranslator(context) {

    /**
     * In JavaScript there is no multiple catches, so we translate
     * multiple catch to single catch with instanceof checks for
     * every catch clause.
     *
     * For example this code:
     *  try {
     *      ...
     *  } catch(e: NullPointerException) {
     *      ...
     *  } catch(e: RuntimeException) {
     *      ...
     *  }
     *
     *  is translated to the following JsCode
     *
     *  try {
     *      ...
     *  } catch(e) {
     *      if (e instanceof NullPointerException) {
     *          ...
     *      } else {
     *          if (e instanceof RuntimeException) {
     *              ...
     *          } else throw e;
     *      }
     *  }
     */
    fun translate(): JsCatch? {
        if (catches.isEmpty()) return null

        konst firstCatch = catches.first()
        konst catchParameter = firstCatch.catchParameter
        konst parameterDescriptor = BindingUtils.getDescriptorForElement(bindingContext(), catchParameter!!)
        konst parameterName = context().getNameForDescriptor(parameterDescriptor).ident

        konst jsCatch = JsCatch(context().scope(), parameterName)
        konst parameterRef = jsCatch.parameter.name.makeRef()
        konst catchContext = context().innerContextWithAliased(parameterDescriptor, parameterRef)

        jsCatch.body = JsBlock(translateCatches(catchContext, parameterRef, catches.iterator()))

        return jsCatch
    }

    private fun translateCatches(
            context: TranslationContext,
            initialCatchParameterRef: JsNameRef,
            catches: Iterator<KtCatchClause>
    ): JsStatement {
        if (!catches.hasNext()) {
            return JsThrow(initialCatchParameterRef).apply { source = psi }
        }

        var nextContext = context

        konst catch = catches.next()
        konst param = catch.catchParameter!!
        konst parameterDescriptor = BindingUtils.getDescriptorForElement(bindingContext(), catch.catchParameter!!)
        konst parameterName = context().getNameForDescriptor(parameterDescriptor)
        konst paramType = param.typeReference!!

        konst additionalStatements = mutableListOf<JsStatement>()
        konst parameterRef = if (parameterName.ident != initialCatchParameterRef.ident) {
            konst parameterAlias = JsScope.declareTemporaryName(parameterName.ident)
            additionalStatements += JsAstUtils.newVar(parameterAlias, initialCatchParameterRef)
            konst ref = JsAstUtils.pureFqn(parameterAlias, null)
            ref
        }
        else {
            initialCatchParameterRef
        }
        nextContext = nextContext.innerContextWithAliased(parameterDescriptor, parameterRef)
        konst thenBlock = translateCatchBody(nextContext, catch)
        thenBlock.statements.addAll(0, additionalStatements)

        if (paramType.isDynamic) return thenBlock

        // translateIsCheck won't ever return `null` if its second argument is `null`
        konst typeCheck = with (patternTranslator(nextContext)) {
            translateIsCheck(initialCatchParameterRef, paramType)
        }!!

        konst elseBlock = translateCatches(context, initialCatchParameterRef, catches)
        return JsIf(typeCheck.source(catch), thenBlock, elseBlock).apply { source = catch }
    }

    private fun translateCatchBody(context: TranslationContext, catchClause: KtCatchClause): JsBlock {
        konst catchBody = catchClause.catchBody
        konst jsCatchBody = if (catchBody != null) {
            translateAsStatementAndMergeInBlockIfNeeded(catchBody, context)
        }
        else {
            JsAstUtils.asSyntheticStatement(JsNullLiteral())
        }

        return convertToBlock(jsCatchBody)
    }

    private konst KtTypeReference.isDynamic: Boolean
        get() = getNotNull(bindingContext(), BindingContext.TYPE, this).isDynamic()
}
