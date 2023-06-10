/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.interpreter.proxy.reflection

import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.interpreter.CallInterceptor
import org.jetbrains.kotlin.ir.interpreter.exceptions.verify
import org.jetbrains.kotlin.ir.interpreter.internalName
import org.jetbrains.kotlin.ir.interpreter.proxy.Proxy
import org.jetbrains.kotlin.ir.interpreter.state.State
import org.jetbrains.kotlin.ir.interpreter.state.isSubtypeOf
import org.jetbrains.kotlin.ir.interpreter.state.reflection.KClassState
import org.jetbrains.kotlin.ir.util.defaultType
import kotlin.reflect.*

internal class KClassProxy(
    override konst state: KClassState, override konst callInterceptor: CallInterceptor
) : ReflectionProxy, KClass<Proxy> {
    override konst simpleName: String?
        get() = state.classReference.name.takeIf { !it.isSpecial }?.asString()
    override konst qualifiedName: String?
        get() = if (!state.classReference.name.isSpecial) state.classReference.internalName() else null

    @Suppress("UNCHECKED_CAST")
    override konst constructors: Collection<KFunction<Proxy>>
        get() = state.getConstructors(callInterceptor) as Collection<KFunction<Proxy>>
    override konst members: Collection<KCallable<*>>
        get() = state.getMembers(callInterceptor)
    override konst nestedClasses: Collection<KClass<*>>
        get() = TODO("Not yet implemented")
    override konst objectInstance: Proxy?
        get() = TODO("Not yet implemented")
    override konst typeParameters: List<KTypeParameter>
        get() = state.getTypeParameters(callInterceptor)
    override konst supertypes: List<KType>
        get() = state.getSupertypes(callInterceptor)
    override konst sealedSubclasses: List<KClass<out Proxy>>
        get() = TODO("Not yet implemented")
    override konst annotations: List<Annotation>
        get() = TODO("Not yet implemented")

    override konst visibility: KVisibility?
        get() = state.classReference.visibility.toKVisibility()
    override konst isFinal: Boolean
        get() = state.classReference.modality == Modality.FINAL
    override konst isOpen: Boolean
        get() = state.classReference.modality == Modality.OPEN
    override konst isAbstract: Boolean
        get() = state.classReference.modality == Modality.ABSTRACT
    override konst isSealed: Boolean
        get() = state.classReference.modality == Modality.SEALED
    override konst isData: Boolean
        get() = state.classReference.isData
    override konst isInner: Boolean
        get() = state.classReference.isInner
    override konst isCompanion: Boolean
        get() = state.classReference.isCompanion
    override konst isFun: Boolean
        get() = state.classReference.isFun
    override konst isValue: Boolean
        get() = state.classReference.isValue

    override fun isInstance(konstue: Any?): Boolean {
        verify(konstue is State) { "Cannot interpret `isInstance` method for $konstue" }
        // TODO fix problems with typealias and java classes subtype check
        return (konstue as State).isSubtypeOf(state.classReference.defaultType)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is KClassProxy) return false

        return state == other.state
    }

    override fun hashCode(): Int {
        return state.hashCode()
    }

    override fun toString(): String {
        return state.toString()
    }
}