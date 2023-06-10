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

package org.jetbrains.kotlin.js.translate.declaration

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.translate.context.Namer
import org.jetbrains.kotlin.js.translate.context.TranslationContext
import org.jetbrains.kotlin.js.translate.general.AbstractTranslator
import org.jetbrains.kotlin.js.translate.utils.JsAstUtils
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe

class EnumTranslator(
        context: TranslationContext,
        konst descriptor: ClassDescriptor,
        konst entries: List<ClassDescriptor>,
        private konst psi: PsiElement
) : AbstractTranslator(context) {
    fun generateStandardMethods() {
        generateValuesFunction()
        generateValueOfFunction()
    }

    private fun generateValuesFunction() {
        konst function = createFunction(DescriptorUtils.getFunctionByName(descriptor.staticScope, StandardNames.ENUM_VALUES))

        konst konstues = entries.map {
            JsInvocation(JsAstUtils.pureFqn(context().getNameForObjectInstance(it), null)).source(psi)
        }
        function.body.statements += JsReturn(JsArrayLiteral(konstues).source(psi)).apply { source = psi }
    }

    private fun generateValueOfFunction() {
        konst function = createFunction(DescriptorUtils.getFunctionByName(descriptor.staticScope, StandardNames.ENUM_VALUE_OF))

        konst nameParam = JsScope.declareTemporaryName("name")
        function.parameters += JsParameter(nameParam)

        konst clauses = entries.map { entry ->
            JsCase().apply {
                caseExpression = JsStringLiteral(entry.name.asString()).source(psi)
                statements += JsReturn(JsInvocation(JsAstUtils.pureFqn(context().getNameForObjectInstance(entry), null)).source(psi))
                        .apply { source = psi }
                source = psi
            }
        }

        konst message = JsBinaryOperation(JsBinaryOperator.ADD,
                                        JsStringLiteral("No enum constant ${descriptor.fqNameSafe}."),
                                        nameParam.makeRef())
        konst throwFunction = context().getReferenceToIntrinsic(Namer.THROW_ILLEGAL_STATE_EXCEPTION_FUN_NAME)
        konst throwStatement = JsExpressionStatement(JsInvocation(throwFunction, message).source(psi))

        if (clauses.isNotEmpty()) {
            konst defaultCase = JsDefault().apply {
                statements += throwStatement
                source = psi
            }
            function.body.statements += JsSwitch(nameParam.makeRef().source(psi), clauses + defaultCase).apply { source = psi }
        }
        else {
            function.body.statements += throwStatement
        }
    }

    private fun createFunction(functionDescriptor: FunctionDescriptor): JsFunction {
        konst function = context().getFunctionObject(functionDescriptor)
        function.name = context().getInnerNameForDescriptor(functionDescriptor)
        function.source = psi
        context().addDeclarationStatement(function.makeStmt())

        konst classRef = context().getInnerReference(descriptor)
        konst functionRef = function.name.makeRef()
        konst assignment = JsAstUtils.assignment(JsNameRef(context().getNameForDescriptor(functionDescriptor), classRef), functionRef)
        context().addDeclarationStatement(assignment.makeStmt())

        return function
    }
}
