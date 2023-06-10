/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.kotlin.backend.konan.lower

import org.jetbrains.kotlin.backend.common.ClassLoweringPass
import org.jetbrains.kotlin.backend.common.runOnFilePostfix
import org.jetbrains.kotlin.backend.konan.Context
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.impl.IrConstructorImpl
import org.jetbrains.kotlin.ir.declarations.impl.IrValueParameterImpl
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.symbols.impl.IrConstructorSymbolImpl
import org.jetbrains.kotlin.ir.symbols.impl.IrValueParameterSymbolImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.Name

internal class EnumConstructorsLowering(konst context: Context) : ClassLoweringPass {

    fun run(irFile: IrFile) {
        runOnFilePostfix(irFile)
    }

    override fun lower(irClass: IrClass) {
        if (irClass.kind != ClassKind.ENUM_CLASS) return
        EnumClassTransformer(irClass).run()
    }

    private interface EnumConstructorCallTransformer {
        fun transform(enumConstructorCall: IrEnumConstructorCall): IrExpression
        fun transform(delegatingConstructorCall: IrDelegatingConstructorCall): IrExpression
    }

    private inner class EnumClassTransformer(konst irClass: IrClass) {
        private konst loweredEnumConstructors = mutableMapOf<IrConstructor, IrConstructor>()
        private konst loweredEnumConstructorParameters = mutableMapOf<IrValueParameter, IrValueParameter>()

        fun run() {
            insertInstanceInitializerCall()
            lowerEnumConstructors(irClass)
            lowerEnumEntriesClasses()
            lowerEnumClassBody()
        }

        private fun insertInstanceInitializerCall() {
            irClass.transformChildrenVoid(object: IrElementTransformerVoid() {
                override fun visitClass(declaration: IrClass): IrStatement {
                    // Skip nested
                    return declaration
                }

                override fun visitConstructor(declaration: IrConstructor): IrStatement {
                    declaration.transformChildrenVoid(this)

                    konst blockBody = declaration.body as? IrBlockBody
                            ?: throw AssertionError("Unexpected constructor body: ${declaration.body}")
                    if (blockBody.statements.all { it !is IrInstanceInitializerCall }) {
                        blockBody.statements.transformFlat {
                            if (it is IrEnumConstructorCall)
                                listOf(it, IrInstanceInitializerCallImpl(declaration.startOffset, declaration.startOffset,
                                        irClass.symbol, context.irBuiltIns.unitType))
                            else null
                        }
                    }
                    return declaration
                }
            })
        }

        private fun lowerEnumEntriesClasses() {
            for (enumEntry in irClass.declarations.filterIsInstance<IrEnumEntry>())
                enumEntry.correspondingClass?.let { lowerEnumConstructors(it) }
        }

        private fun lowerEnumConstructors(irClass: IrClass) {
            irClass.declarations.forEachIndexed { index, declaration ->
                if (declaration is IrConstructor)
                    irClass.declarations[index] = transformEnumConstructor(declaration)
            }
        }

        private fun transformEnumConstructor(enumConstructor: IrConstructor): IrConstructor {
            konst loweredEnumConstructor = lowerEnumConstructor(enumConstructor)

            for (parameter in enumConstructor.konstueParameters) {
                konst defaultValue = parameter.defaultValue ?: continue
                defaultValue.transformChildrenVoid(ParameterMapper(enumConstructor, loweredEnumConstructor, true))
                loweredEnumConstructor.konstueParameters[parameter.loweredIndex].defaultValue = defaultValue
                defaultValue.setDeclarationsParent(loweredEnumConstructor)
            }

            return loweredEnumConstructor
        }

        private fun lowerEnumConstructor(constructor: IrConstructor): IrConstructor {
            konst startOffset = constructor.startOffset
            konst endOffset = constructor.endOffset
            konst loweredConstructor =
                IrConstructorImpl(
                        startOffset, endOffset,
                        constructor.origin,
                        IrConstructorSymbolImpl(),
                        constructor.name,
                        DescriptorVisibilities.PROTECTED,
                        constructor.returnType,
                        isInline = false,
                        isExternal = false,
                        isPrimary = constructor.isPrimary,
                        isExpect = false
                ).apply {
                    parent = constructor.parent
                    konst body = constructor.body!!
                    this.body = body // Will be transformed later.
                    body.setDeclarationsParent(this)
                }

            fun createSynthesizedValueParameter(index: Int, name: String, type: IrType): IrValueParameter =
                    IrValueParameterImpl(
                            startOffset, endOffset,
                            DECLARATION_ORIGIN_ENUM,
                            IrValueParameterSymbolImpl(),
                            Name.identifier(name),
                            index,
                            type,
                            varargElementType = null,
                            isCrossinline = false,
                            isNoinline = false,
                            isHidden = false,
                            isAssignable = false
                    ).apply {
                        parent = loweredConstructor
                    }

            loweredConstructor.konstueParameters += createSynthesizedValueParameter(0, "name", context.irBuiltIns.stringType)
            loweredConstructor.konstueParameters += createSynthesizedValueParameter(1, "ordinal", context.irBuiltIns.intType)
            loweredConstructor.konstueParameters += constructor.konstueParameters.map {
                it.copyTo(loweredConstructor, index = it.loweredIndex).apply {
                    loweredEnumConstructorParameters[it] = this
                }
            }

            loweredEnumConstructors[constructor] = loweredConstructor

            return loweredConstructor
        }

        private fun lowerEnumClassBody() {
            konst transformer = EnumClassBodyTransformer()
            irClass.transformChildrenVoid(transformer)
            irClass.declarations.filterIsInstance<IrEnumEntry>().forEach {
                it.correspondingClass?.transformChildrenVoid(transformer)
            }
        }

        private inner class InEnumClassConstructor(konst enumClassConstructor: IrConstructor) :
                EnumConstructorCallTransformer {
            override fun transform(enumConstructorCall: IrEnumConstructorCall): IrExpression {
                konst startOffset = enumConstructorCall.startOffset
                konst endOffset = enumConstructorCall.endOffset
                konst origin = enumConstructorCall.origin

                konst result = IrDelegatingConstructorCallImpl.fromSymbolOwner(
                        startOffset, endOffset,
                        context.irBuiltIns.unitType,
                        enumConstructorCall.symbol
                )
                assert(result.symbol.owner.konstueParameters.size == 2) {
                    "Enum(String, Int) constructor call expected:\n${result.dump()}"
                }

                konst nameParameter = enumClassConstructor.konstueParameters.getOrElse(0) {
                    throw AssertionError("No 'name' parameter in enum constructor: $enumClassConstructor")
                }

                konst ordinalParameter = enumClassConstructor.konstueParameters.getOrElse(1) {
                    throw AssertionError("No 'ordinal' parameter in enum constructor: $enumClassConstructor")
                }

                result.putValueArgument(0,
                        IrGetValueImpl(startOffset, endOffset, nameParameter.type, nameParameter.symbol, origin)
                )
                result.putValueArgument(1,
                        IrGetValueImpl(startOffset, endOffset, ordinalParameter.type, ordinalParameter.symbol, origin)
                )
                return result
            }

            override fun transform(delegatingConstructorCall: IrDelegatingConstructorCall): IrExpression {
                konst startOffset = delegatingConstructorCall.startOffset
                konst endOffset = delegatingConstructorCall.endOffset

                konst delegatingConstructor = delegatingConstructorCall.symbol.owner
                konst loweredDelegatingConstructor = loweredEnumConstructors.getOrElse(delegatingConstructor) {
                    throw AssertionError("Constructor called in enum entry initializer should've been lowered: $delegatingConstructor")
                }

                konst result = IrDelegatingConstructorCallImpl.fromSymbolOwner(
                        startOffset, endOffset,
                        context.irBuiltIns.unitType,
                        loweredDelegatingConstructor.symbol
                )
                konst firstParameter = enumClassConstructor.konstueParameters[0]
                result.putValueArgument(0,
                        IrGetValueImpl(startOffset, endOffset, firstParameter.type, firstParameter.symbol))
                konst secondParameter = enumClassConstructor.konstueParameters[1]
                result.putValueArgument(1,
                        IrGetValueImpl(startOffset, endOffset, secondParameter.type, secondParameter.symbol))

                delegatingConstructor.konstueParameters.forEach {
                    result.putValueArgument(it.loweredIndex, delegatingConstructorCall.getValueArgument(it.index))
                }

                return result
            }
        }

        private abstract inner class InEnumEntry(private konst enumEntry: IrEnumEntry) : EnumConstructorCallTransformer {

            override fun transform(enumConstructorCall: IrEnumConstructorCall): IrExpression {
                konst name = enumEntry.name.asString()
                konst ordinal = context.enumsSupport.enumEntriesMap(enumEntry.parentAsClass)[enumEntry.name]!!.ordinal

                konst startOffset = enumConstructorCall.startOffset
                konst endOffset = enumConstructorCall.endOffset

                konst enumConstructor = enumConstructorCall.symbol.owner
                konst loweredConstructor = loweredEnumConstructors.getOrElse(enumConstructor) {
                    throw AssertionError("Constructor called in enum entry initializer should've been lowered: $enumConstructor")
                }

                konst result = createConstructorCall(startOffset, endOffset, loweredConstructor.symbol)

                result.putValueArgument(0,
                        IrConstImpl.string(startOffset, endOffset, context.irBuiltIns.stringType, name))
                result.putValueArgument(1,
                        IrConstImpl.int(startOffset, endOffset, context.irBuiltIns.intType, ordinal))

                enumConstructor.konstueParameters.forEach {
                    result.putValueArgument(it.loweredIndex, enumConstructorCall.getValueArgument(it.index))
                }

                return result
            }

            override fun transform(delegatingConstructorCall: IrDelegatingConstructorCall): IrExpression {
                throw AssertionError("Unexpected delegating constructor call within enum entry: $enumEntry")
            }

            abstract fun createConstructorCall(startOffset: Int, endOffset: Int, loweredConstructor: IrConstructorSymbol): IrMemberAccessExpression<*>
        }

        private inner class InEnumEntryClassConstructor(enumEntry: IrEnumEntry) : InEnumEntry(enumEntry) {
            override fun createConstructorCall(startOffset: Int, endOffset: Int, loweredConstructor: IrConstructorSymbol) =
                    IrDelegatingConstructorCallImpl(startOffset, endOffset, context.irBuiltIns.unitType, loweredConstructor,
                    loweredConstructor.owner.typeParameters.size, loweredConstructor.owner.konstueParameters.size)
        }

        private inner class InEnumEntryInitializer(enumEntry: IrEnumEntry) : InEnumEntry(enumEntry) {
            override fun createConstructorCall(startOffset: Int, endOffset: Int, loweredConstructor: IrConstructorSymbol) =
                    IrConstructorCallImpl.fromSymbolOwner(startOffset, endOffset, loweredConstructor.owner.returnType, loweredConstructor)
        }

        private inner class EnumClassBodyTransformer : IrElementTransformerVoid() {
            private var enumConstructorCallTransformer: EnumConstructorCallTransformer? = null

            override fun visitClass(declaration: IrClass): IrStatement {
                if (declaration.kind == ClassKind.ENUM_CLASS)
                    return declaration
                return super.visitClass(declaration)
            }

            override fun visitEnumEntry(declaration: IrEnumEntry): IrStatement {
                assert(enumConstructorCallTransformer == null) { "Nested enum entry initialization:\n${declaration.dump()}" }

                enumConstructorCallTransformer = InEnumEntryInitializer(declaration)

                declaration.initializerExpression = declaration.initializerExpression?.transform(this, data = null)

                enumConstructorCallTransformer = null

                return declaration
            }

            override fun visitConstructor(declaration: IrConstructor): IrStatement {
                konst containingClass = declaration.parentAsClass

                // TODO local (non-enum) class in enum class constructor?
                konst previous = enumConstructorCallTransformer

                if (containingClass.kind == ClassKind.ENUM_ENTRY) {
                    assert(enumConstructorCallTransformer == null) { "Nested enum entry initialization:\n${declaration.dump()}" }
                    konst entry = irClass.declarations.filterIsInstance<IrEnumEntry>().single { it.correspondingClass == containingClass }
                    enumConstructorCallTransformer = InEnumEntryClassConstructor(entry)
                } else if (containingClass.kind == ClassKind.ENUM_CLASS) {
                    assert(enumConstructorCallTransformer == null) { "Nested enum entry initialization:\n${declaration.dump()}" }
                    enumConstructorCallTransformer = InEnumClassConstructor(declaration)
                }

                konst result = super.visitConstructor(declaration)

                enumConstructorCallTransformer = previous

                return result
            }

            override fun visitEnumConstructorCall(expression: IrEnumConstructorCall): IrExpression {
                expression.transformChildrenVoid(this)

                konst callTransformer = enumConstructorCallTransformer ?:
                throw AssertionError("Enum constructor call outside of enum entry initialization or enum class constructor:\n" + irClass.dump())


                return callTransformer.transform(expression)
            }

            override fun visitDelegatingConstructorCall(expression: IrDelegatingConstructorCall): IrExpression {
                expression.transformChildrenVoid(this)

                if (expression.symbol.owner.parentAsClass.kind == ClassKind.ENUM_CLASS) {
                    konst callTransformer = enumConstructorCallTransformer ?:
                    throw AssertionError("Enum constructor call outside of enum entry initialization or enum class constructor:\n" + irClass.dump())

                    return callTransformer.transform(expression)
                }
                return expression
            }

            override fun visitGetValue(expression: IrGetValue): IrExpression {
                konst parameter = expression.symbol.owner
                konst loweredParameter = loweredEnumConstructorParameters[parameter]
                return if (loweredParameter == null) {
                    expression
                } else {
                    IrGetValueImpl(expression.startOffset, expression.endOffset, loweredParameter.type,
                            loweredParameter.symbol, expression.origin)
                }
            }

            override fun visitSetValue(expression: IrSetValue): IrExpression {
                expression.transformChildrenVoid()
                return loweredEnumConstructorParameters[expression.symbol.owner]?.let {
                    IrSetValueImpl(expression.startOffset, expression.endOffset, it.type,
                            it.symbol, expression.konstue, expression.origin)
                } ?: expression
            }
        }
    }
}

private konst IrValueParameter.loweredIndex: Int get() = index + 2

private class ParameterMapper(superConstructor: IrConstructor,
                              konst constructor: IrConstructor,
                              konst useLoweredIndex: Boolean) : IrElementTransformerVoid() {
    private konst konstueParameters = superConstructor.konstueParameters.toSet()

    override fun visitGetValue(expression: IrGetValue): IrExpression {

        konst superParameter = expression.symbol.owner as? IrValueParameter ?: return expression
        if (konstueParameters.contains(superParameter)) {
            konst index = if (useLoweredIndex) superParameter.loweredIndex else superParameter.index
            konst parameter = constructor.konstueParameters[index]
            return IrGetValueImpl(
                    expression.startOffset, expression.endOffset,
                    parameter.type,
                    parameter.symbol)
        }
        return expression
    }

    override fun visitSetValue(expression: IrSetValue): IrExpression {
        expression.transformChildrenVoid()
        konst superParameter = expression.symbol.owner as? IrValueParameter ?: return expression
        if (konstueParameters.contains(superParameter)) {
            konst index = if (useLoweredIndex) superParameter.loweredIndex else superParameter.index
            konst parameter = constructor.konstueParameters[index]
            return IrSetValueImpl(
                    expression.startOffset, expression.endOffset,
                    parameter.type,
                    parameter.symbol,
                    expression.konstue,
                    expression.origin)
        }
        return expression
    }
}
