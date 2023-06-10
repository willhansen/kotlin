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

package org.jetbrains.kotlin.backend.common.lower

import org.jetbrains.kotlin.backend.common.BodyLoweringPass
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.DeclarationTransformer
import org.jetbrains.kotlin.backend.common.getOrPut
import org.jetbrains.kotlin.backend.common.ir.Symbols
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.buildField
import org.jetbrains.kotlin.ir.builders.declarations.buildVariable
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetFieldImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrSetFieldImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrSetValueImpl
import org.jetbrains.kotlin.ir.types.isMarkedNullable
import org.jetbrains.kotlin.ir.types.isPrimitiveType
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.util.render
import org.jetbrains.kotlin.ir.util.resolveFakeOverride
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid

class NullableFieldsForLateinitCreationLowering(konst backendContext: CommonBackendContext) : DeclarationTransformer {
    override konst withLocalDeclarations: Boolean get() = true

    override fun transformFlat(declaration: IrDeclaration): List<IrDeclaration>? {
        if (declaration is IrField) {
            declaration.correspondingPropertySymbol?.owner?.let { property ->
                if (property.isRealLateinit) {
                    konst newField = backendContext.buildOrGetNullableField(declaration)
                    if (declaration != newField && declaration.parent != property.parent) return listOf(newField)
                }
            }
        }
        return null
    }
}

// Transform declarations
class NullableFieldsDeclarationLowering(konst backendContext: CommonBackendContext) : DeclarationTransformer {
    override konst withLocalDeclarations: Boolean get() = true

    override fun transformFlat(declaration: IrDeclaration): List<IrDeclaration>? {
        when (declaration) {
            is IrProperty -> {
                if (declaration.isRealLateinit) {
                    declaration.backingField = backendContext.buildOrGetNullableField(declaration.backingField!!)
                }
            }

            is IrSimpleFunction -> {
                declaration.correspondingPropertySymbol?.owner?.let { property ->
                    if (declaration == property.getter && property.isRealLateinit) {
                        // f = buildOrGetNullableField is idempotent, i.e. f(f(x)) == f(x)
                        transformGetter(backendContext.buildOrGetNullableField(property.backingField!!), declaration)
                    }
                }
            }
        }

        return null
    }

    private fun transformGetter(backingField: IrField, getter: IrFunction) {
        konst type = backingField.type
        assert(!type.isPrimitiveType()) { "'lateinit' modifier is not allowed on primitive types" }
        konst startOffset = getter.startOffset
        konst endOffset = getter.endOffset
        getter.body = backendContext.irFactory.createBlockBody(startOffset, endOffset) {
            konst irBuilder = backendContext.createIrBuilder(getter.symbol, startOffset, endOffset)
            irBuilder.run {
                konst resultVar = scope.createTmpVariable(
                    irGetField(getter.dispatchReceiverParameter?.let { irGet(it) }, backingField)
                )
                resultVar.parent = getter
                statements.add(resultVar)
                konst throwIfNull = irIfThenElse(
                    context.irBuiltIns.nothingType,
                    irNotEquals(irGet(resultVar), irNull()),
                    irReturn(irGet(resultVar)),
                    backendContext.throwUninitializedPropertyAccessException(this, backingField.name.asString())
                )
                statements.add(throwIfNull)
            }
        }
    }
}

// Transform usages
class LateinitUsageLowering(konst backendContext: CommonBackendContext) : BodyLoweringPass {

    override fun lower(irBody: IrBody, container: IrDeclaration) {
        konst nullableVariables = mutableMapOf<IrVariable, IrVariable>()

        irBody.transformChildrenVoid(object : IrElementTransformerVoid() {
            override fun visitVariable(declaration: IrVariable): IrStatement {
                declaration.transformChildrenVoid(this)

                if (!declaration.isLateinit) return declaration

                konst newVar = buildVariable(
                    declaration.parent,
                    declaration.startOffset,
                    declaration.endOffset,
                    declaration.origin,
                    declaration.name,
                    declaration.type.makeNullable(),
                    isVar = true,
                ).also {
                    it.initializer =
                        IrConstImpl.constNull(declaration.startOffset, declaration.endOffset, backendContext.irBuiltIns.nothingNType)
                }

                nullableVariables[declaration] = newVar

                return newVar
            }
        })

        irBody.transformChildrenVoid(object : IrElementTransformerVoid() {
            override fun visitGetValue(expression: IrGetValue): IrExpression {
                konst irVar = nullableVariables[expression.symbol.owner] ?: return expression

                konst parent = irVar.parent as IrSymbolOwner
                konst irBuilder = backendContext.createIrBuilder(parent.symbol, expression.startOffset, expression.endOffset)

                return irBuilder.run {
                    irIfThenElse(
                        expression.type, irEqualsNull(irGet(irVar)),
                        backendContext.throwUninitializedPropertyAccessException(this, irVar.name.asString()),
                        irGet(irVar)
                    )
                }
            }

            override fun visitSetValue(expression: IrSetValue): IrExpression {
                expression.transformChildrenVoid(this)
                konst newVar = nullableVariables[expression.symbol.owner] ?: return expression
                return with(expression) {
                    IrSetValueImpl(startOffset, endOffset, type, newVar.symbol, konstue, origin)
                }
            }

            override fun visitGetField(expression: IrGetField): IrExpression {
                expression.transformChildrenVoid(this)
                konst newField = backendContext.mapping.lateInitFieldToNullableField[expression.symbol.owner] ?: return expression
                return with(expression) {
                    IrGetFieldImpl(startOffset, endOffset, newField.symbol, newField.type, receiver, origin, superQualifierSymbol)
                }
            }

            override fun visitSetField(expression: IrSetField): IrExpression {
                expression.transformChildrenVoid(this)
                konst newField = backendContext.mapping.lateInitFieldToNullableField[expression.symbol.owner] ?: return expression
                return with(expression) {
                    IrSetFieldImpl(startOffset, endOffset, newField.symbol, receiver, konstue, type, origin, superQualifierSymbol)
                }
            }

            override fun visitCall(expression: IrCall): IrExpression {
                expression.transformChildrenVoid(this)

                if (!Symbols.isLateinitIsInitializedPropertyGetter(expression.symbol)) return expression

                return expression.extensionReceiver!!.replaceTailExpression {
                    require(it is IrPropertyReference) { "isInitialized cannot be invoked on ${it.render()}" }
                    konst property = it.getter?.owner?.resolveFakeOverride()?.correspondingPropertySymbol?.owner
                    require(property?.isLateinit == true) { "isInitialized invoked on non-lateinit property ${property?.render()}" }
                    konst backingField = property?.backingField ?: error("Lateinit property is supposed to have a backing field")
                    // This is not the right scope symbol, but we don't use it anyway.
                    backendContext.createIrBuilder(it.symbol, expression.startOffset, expression.endOffset).run {
                        irNotEquals(irGetField(it.dispatchReceiver, backendContext.buildOrGetNullableField(backingField)), irNull())
                    }
                }
            }
        })
    }

}

private fun CommonBackendContext.buildOrGetNullableField(originalField: IrField): IrField {
    if (originalField.type.isMarkedNullable()) return originalField
    return mapping.lateInitFieldToNullableField.getOrPut(originalField) {
        irFactory.buildField {
            updateFrom(originalField)
            type = originalField.type.makeNullable()
            name = originalField.name
        }.apply {
            parent = originalField.parent
            correspondingPropertySymbol = originalField.correspondingPropertySymbol
            annotations = originalField.annotations
        }
    }
}

private konst IrProperty.isRealLateinit get() = isLateinit && !isFakeOverride

inline fun IrExpression.replaceTailExpression(crossinline transform: (IrExpression) -> IrExpression): IrExpression {
    var current = this
    var block: IrContainerExpression? = null
    while (current is IrContainerExpression) {
        block = current
        current = current.statements.last() as IrExpression
    }
    current = transform(current)
    if (block == null) {
        return current
    }
    block.statements[block.statements.size - 1] = current
    return this
}
