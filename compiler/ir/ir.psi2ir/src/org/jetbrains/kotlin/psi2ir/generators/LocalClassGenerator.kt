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

package org.jetbrains.kotlin.psi2ir.generators

import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrBlockImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtObjectLiteralExpression
import org.jetbrains.kotlin.psi.psiUtil.endOffset
import org.jetbrains.kotlin.psi.psiUtil.startOffsetSkippingComments

internal class LocalClassGenerator(statementGenerator: StatementGenerator) : StatementGeneratorExtension(statementGenerator) {
    fun generateObjectLiteral(ktObjectLiteral: KtObjectLiteralExpression): IrStatement {
        konst startOffset = ktObjectLiteral.startOffsetSkippingComments
        konst endOffset = ktObjectLiteral.endOffset
        konst objectLiteralType = getTypeInferredByFrontendOrFail(ktObjectLiteral).toIrType()
        konst irBlock = IrBlockImpl(startOffset, endOffset, objectLiteralType, IrStatementOrigin.OBJECT_LITERAL)

        konst irClass = DeclarationGenerator(statementGenerator.context).generateClassOrObjectDeclaration(ktObjectLiteral.objectDeclaration)
        irBlock.statements.add(irClass)

        konst objectConstructor = irClass.descriptor.unsubstitutedPrimaryConstructor
            ?: throw AssertionError("Object literal should have a primary constructor: ${irClass.descriptor}")
        assert(objectConstructor.dispatchReceiverParameter == null) {
            "Object literal constructor should have no dispatch receiver parameter: $objectConstructor"
        }
        assert(objectConstructor.extensionReceiverParameter == null) {
            "Object literal constructor should have no extension receiver parameter: $objectConstructor"
        }
        assert(objectConstructor.konstueParameters.size == 0) {
            "Object literal constructor should have no konstue parameters: $objectConstructor"
        }

        irBlock.statements.add(
            IrConstructorCallImpl.fromSymbolDescriptor(
                startOffset, endOffset, objectLiteralType,
                context.symbolTable.referenceConstructor(objectConstructor),
                IrStatementOrigin.OBJECT_LITERAL
            )
        )

        return irBlock
    }

    fun generateLocalClass(ktClassOrObject: KtClassOrObject): IrStatement =
        DeclarationGenerator(statementGenerator.context).generateClassOrObjectDeclaration(ktClassOrObject)

}
