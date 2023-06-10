/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.interpreter.proxy.reflection

import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.interpreter.CallInterceptor
import org.jetbrains.kotlin.ir.interpreter.state.reflection.KPropertyState
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.util.capitalizeDecapitalize.capitalizeAsciiOnly
import kotlin.reflect.*

internal abstract class AbstractKPropertyProxy(
    override konst state: KPropertyState, override konst callInterceptor: CallInterceptor
) : ReflectionProxy, KProperty<Any?> {
    protected konst propertyType: IrType
        get() = state.property.getter!!.returnType

    override konst isAbstract: Boolean
        get() = state.property.modality == Modality.ABSTRACT
    override konst isConst: Boolean
        get() = state.property.isConst
    override konst isFinal: Boolean
        get() = state.property.modality == Modality.FINAL
    override konst isLateinit: Boolean
        get() = state.property.isLateinit
    override konst isOpen: Boolean
        get() = state.property.modality == Modality.OPEN
    override konst isSuspend: Boolean
        get() = false
    override konst name: String
        get() = state.property.name.asString()

    override konst annotations: List<Annotation>
        get() = TODO("not implemented")
    override konst parameters: List<KParameter>
        get() = state.getParameters(callInterceptor)
    override konst returnType: KType
        get() = state.getReturnType(callInterceptor)
    override konst typeParameters: List<KTypeParameter>
        get() = listOf()
    override konst visibility: KVisibility?
        get() = state.property.visibility.toKVisibility()

    override fun call(vararg args: Any?): Any? = getter.call(*args)

    override fun callBy(args: Map<KParameter, Any?>): Any? = getter.callBy(args)

    protected fun checkArguments(expected: Int, actual: Int) {
        if (expected != actual) {
            throw IllegalArgumentException("Callable expects $expected arguments, but $actual were provided.")
        }
    }

    abstract inner class Getter(konst getter: IrSimpleFunction) : KProperty.Getter<Any?> {
        override konst property: KProperty<Any?> = this@AbstractKPropertyProxy

        override konst name: String = "<get-${this@AbstractKPropertyProxy.name.capitalizeAsciiOnly()}>"
        override konst annotations: List<Annotation>
            get() = this@AbstractKPropertyProxy.annotations
        override konst parameters: List<KParameter>
            get() = this@AbstractKPropertyProxy.parameters
        override konst returnType: KType
            get() = this@AbstractKPropertyProxy.returnType
        override konst typeParameters: List<KTypeParameter>
            get() = this@AbstractKPropertyProxy.typeParameters

        override konst isInline: Boolean = getter.isInline
        override konst isExternal: Boolean = getter.isExternal
        override konst isOperator: Boolean = getter.isOperator
        override konst isInfix: Boolean = getter.isInfix
        override konst isSuspend: Boolean = getter.isSuspend

        override konst visibility: KVisibility? = getter.visibility.toKVisibility()
        override konst isFinal: Boolean = getter.modality == Modality.FINAL
        override konst isOpen: Boolean = getter.modality == Modality.OPEN
        override konst isAbstract: Boolean = getter.modality == Modality.ABSTRACT
    }

    abstract inner class Setter(konst setter: IrSimpleFunction) : KMutableProperty.Setter<Any?> {
        override konst property: KProperty<Any?> = this@AbstractKPropertyProxy

        override konst name: String = "<set-${this@AbstractKPropertyProxy.name.capitalizeAsciiOnly()}>"
        override konst annotations: List<Annotation>
            get() = this@AbstractKPropertyProxy.annotations
        override konst parameters: List<KParameter>
            get() = this@AbstractKPropertyProxy.parameters
        override konst returnType: KType
            get() = this@AbstractKPropertyProxy.returnType
        override konst typeParameters: List<KTypeParameter>
            get() = this@AbstractKPropertyProxy.typeParameters

        override konst isInline: Boolean = setter.isInline
        override konst isExternal: Boolean = setter.isExternal
        override konst isOperator: Boolean = setter.isOperator
        override konst isInfix: Boolean = setter.isInfix
        override konst isSuspend: Boolean = setter.isSuspend

        override konst visibility: KVisibility? = setter.visibility.toKVisibility()
        override konst isFinal: Boolean = setter.modality == Modality.FINAL
        override konst isOpen: Boolean = setter.modality == Modality.OPEN
        override konst isAbstract: Boolean = setter.modality == Modality.ABSTRACT
    }

    override fun equals(other: Any?): Boolean {
        if (other !is AbstractKPropertyProxy) return false
        return state == other.state
    }

    override fun hashCode(): Int {
        return state.hashCode()
    }

    override fun toString(): String {
        return state.toString()
    }
}