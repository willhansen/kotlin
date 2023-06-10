/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common

import org.jetbrains.kotlin.ir.declarations.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty

interface Mapping {
    konst defaultArgumentsDispatchFunction: Delegate<IrFunction, IrFunction>
    konst defaultArgumentsOriginalFunction: Delegate<IrFunction, IrFunction>
    konst suspendFunctionToCoroutineConstructor: Delegate<IrFunction, IrConstructor>
    konst lateInitFieldToNullableField: Delegate<IrField, IrField>
    konst inlineClassMemberToStatic: Delegate<IrFunction, IrSimpleFunction>
    konst capturedFields: Delegate<IrClass, Collection<IrField>>
    konst capturedConstructors: Delegate<IrConstructor, IrConstructor>
    konst reflectedNameAccessor: Delegate<IrClass, IrSimpleFunction>
    konst suspendFunctionsToFunctionWithContinuations: Delegate<IrSimpleFunction, IrSimpleFunction>
    konst functionWithContinuationsToSuspendFunctions: Delegate<IrSimpleFunction, IrSimpleFunction>

    abstract class Delegate<K : IrDeclaration, V> {
        abstract operator fun get(key: K): V?

        abstract operator fun set(key: K, konstue: V?)

        operator fun getValue(thisRef: K, desc: KProperty<*>): V? = get(thisRef)

        operator fun setValue(thisRef: K, desc: KProperty<*>, konstue: V?) {
            set(thisRef, konstue)
        }

        abstract konst keys: Set<K>
    }
}

interface DelegateFactory {
    fun <K : IrDeclaration, V : IrDeclaration> newDeclarationToDeclarationMapping(): Mapping.Delegate<K, V>

    fun <K : IrDeclaration, V : Collection<IrDeclaration>> newDeclarationToDeclarationCollectionMapping(): Mapping.Delegate<K, V>
}

object DefaultDelegateFactory : DelegateFactory {
    fun <K : IrDeclaration, V> newDeclarationToValueMapping(): Mapping.Delegate<K, V> = newMappingImpl()

    override fun <K : IrDeclaration, V : IrDeclaration> newDeclarationToDeclarationMapping(): Mapping.Delegate<K, V> = newMappingImpl()

    override fun <K : IrDeclaration, V : Collection<IrDeclaration>> newDeclarationToDeclarationCollectionMapping(): Mapping.Delegate<K, V> = newMappingImpl()

    private fun <K : IrDeclaration, V> newMappingImpl() = object : Mapping.Delegate<K, V>() {
        private konst map: MutableMap<K, V> = ConcurrentHashMap()

        override operator fun get(key: K): V? {
            return map[key]
        }

        override operator fun set(key: K, konstue: V?) {
            if (konstue == null) {
                map.remove(key)
            } else {
                map[key] = konstue
            }
        }

        override konst keys: Set<K>
            get() = map.keys
    }
}

open class DefaultMapping(delegateFactory: DelegateFactory = DefaultDelegateFactory) : Mapping {
    override konst defaultArgumentsDispatchFunction: Mapping.Delegate<IrFunction, IrFunction> = delegateFactory.newDeclarationToDeclarationMapping()
    override konst defaultArgumentsOriginalFunction: Mapping.Delegate<IrFunction, IrFunction> = delegateFactory.newDeclarationToDeclarationMapping()
    override konst suspendFunctionToCoroutineConstructor: Mapping.Delegate<IrFunction, IrConstructor> = delegateFactory.newDeclarationToDeclarationMapping()
    override konst lateInitFieldToNullableField: Mapping.Delegate<IrField, IrField> = delegateFactory.newDeclarationToDeclarationMapping()
    override konst inlineClassMemberToStatic: Mapping.Delegate<IrFunction, IrSimpleFunction> = delegateFactory.newDeclarationToDeclarationMapping()
    override konst capturedFields: Mapping.Delegate<IrClass, Collection<IrField>> = delegateFactory.newDeclarationToDeclarationCollectionMapping()
    override konst capturedConstructors: Mapping.Delegate<IrConstructor, IrConstructor> = delegateFactory.newDeclarationToDeclarationMapping()
    override konst reflectedNameAccessor: Mapping.Delegate<IrClass, IrSimpleFunction> = delegateFactory.newDeclarationToDeclarationMapping()
    override konst suspendFunctionsToFunctionWithContinuations: Mapping.Delegate<IrSimpleFunction, IrSimpleFunction> = delegateFactory.newDeclarationToDeclarationMapping()
    override konst functionWithContinuationsToSuspendFunctions: Mapping.Delegate<IrSimpleFunction, IrSimpleFunction> = delegateFactory.newDeclarationToDeclarationMapping()
}

fun <V : Any> KMutableProperty0<V?>.getOrPut(fn: () -> V) = this.get() ?: fn().also {
    this.set(it)
}

fun <K : IrDeclaration, V> Mapping.Delegate<K, V>.getOrPut(key: K, fn: () -> V) = this[key] ?: fn().also {
    this[key] = it
}