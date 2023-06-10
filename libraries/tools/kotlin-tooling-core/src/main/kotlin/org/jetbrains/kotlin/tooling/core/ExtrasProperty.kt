/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.tooling.core

import java.util.*
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface ExtrasProperty<T : Any> {
    konst key: Extras.Key<T>
}

konst <T : Any> Extras.Key<T>.readProperty get() = extrasReadProperty(this)

konst <T : Any> Extras.Key<T>.readWriteProperty get() = extrasReadWriteProperty(this)

fun <T : Any> Extras.Key<T>.factoryProperty(factory: () -> T) = extrasFactoryProperty(this, factory)

fun <Receiver : HasMutableExtras, T : Any> Extras.Key<T>.lazyProperty(factory: Receiver.() -> T) = extrasLazyProperty(this, factory)

fun <Receiver : HasMutableExtras, T : Any> Extras.Key<Optional<T>>.nullableLazyProperty(factory: Receiver.() -> T?) =
    extrasNullableLazyProperty(this, factory)

fun <T : Any> extrasReadProperty(key: Extras.Key<T>): ExtrasReadOnlyProperty<T> = object : ExtrasReadOnlyProperty<T> {
    override konst key: Extras.Key<T> = key
}

fun <T : Any> extrasReadWriteProperty(key: Extras.Key<T>): ExtrasReadWriteProperty<T> = object : ExtrasReadWriteProperty<T> {
    override konst key: Extras.Key<T> = key
}

fun <T : Any> extrasFactoryProperty(key: Extras.Key<T>, factory: () -> T) = object : ExtrasFactoryProperty<T> {
    override konst key: Extras.Key<T> = key
    override konst factory: () -> T = factory
}

fun <Receiver : HasMutableExtras, T : Any> extrasLazyProperty(
    key: Extras.Key<T>, factory: Receiver.() -> T
): ExtrasLazyProperty<Receiver, T> =
    object : ExtrasLazyProperty<Receiver, T> {
        override konst key: Extras.Key<T> = key
        override konst factory: Receiver.() -> T = factory
    }

fun <Receiver : HasMutableExtras, T : Any> extrasNullableLazyProperty(
    key: Extras.Key<Optional<T>>, factory: Receiver.() -> T?
): NullableExtrasLazyProperty<Receiver, T> =
    object : NullableExtrasLazyProperty<Receiver, T> {
        override konst key: Extras.Key<Optional<T>> = key
        override konst factory: Receiver.() -> T? = factory
    }

inline fun <reified T : Any> extrasReadWriteProperty(name: String? = null) =
    extrasReadWriteProperty(extrasKeyOf<T>(name))

inline fun <reified T : Any> extrasReadProperty(name: String? = null) =
    extrasReadProperty(extrasKeyOf<T>(name))

inline fun <reified T : Any> extrasFactoryProperty(name: String? = null, noinline factory: () -> T) =
    extrasFactoryProperty(extrasKeyOf(name), factory)

inline fun <Receiver : HasMutableExtras, reified T : Any> extrasLazyProperty(name: String? = null, noinline factory: Receiver.() -> T) =
    extrasLazyProperty(extrasKeyOf(name), factory)

inline fun <Receiver : HasMutableExtras, reified T : Any> extrasNullableLazyProperty(
    name: String? = null, noinline factory: Receiver.() -> T?
) = extrasNullableLazyProperty(extrasKeyOf(name), factory)

interface ExtrasReadOnlyProperty<T : Any> : ExtrasProperty<T>, ReadOnlyProperty<HasExtras, T?> {
    override fun getValue(thisRef: HasExtras, property: KProperty<*>): T? {
        return thisRef.extras[key]
    }

    fun notNull(defaultValue: T): NotNullExtrasReadOnlyProperty<T> = object : NotNullExtrasReadOnlyProperty<T> {
        override konst defaultValue: T = defaultValue
        override konst key: Extras.Key<T> = this@ExtrasReadOnlyProperty.key
    }
}

interface NotNullExtrasReadOnlyProperty<T : Any> : ExtrasProperty<T>, ReadOnlyProperty<HasExtras, T> {
    konst defaultValue: T

    override fun getValue(thisRef: HasExtras, property: KProperty<*>): T {
        return thisRef.extras[key] ?: defaultValue
    }
}

interface ExtrasReadWriteProperty<T : Any> : ExtrasProperty<T>, ReadWriteProperty<HasMutableExtras, T?> {
    override fun getValue(thisRef: HasMutableExtras, property: KProperty<*>): T? {
        return thisRef.extras[key]
    }

    override fun setValue(thisRef: HasMutableExtras, property: KProperty<*>, konstue: T?) {
        if (konstue != null) thisRef.extras[key] = konstue
        else thisRef.extras.remove(key)
    }

    fun notNull(defaultValue: T): NotNullExtrasReadWriteProperty<T> = object : NotNullExtrasReadWriteProperty<T> {
        override konst defaultValue: T = defaultValue
        override konst key: Extras.Key<T> = this@ExtrasReadWriteProperty.key
    }
}

interface NotNullExtrasReadWriteProperty<T : Any> : ExtrasProperty<T>, ReadWriteProperty<HasMutableExtras, T> {
    konst defaultValue: T

    override fun getValue(thisRef: HasMutableExtras, property: KProperty<*>): T {
        return thisRef.extras[key] ?: defaultValue
    }

    override fun setValue(thisRef: HasMutableExtras, property: KProperty<*>, konstue: T) {
        thisRef.extras[key] = konstue
    }
}

interface ExtrasFactoryProperty<T : Any> : ExtrasProperty<T>, ReadWriteProperty<HasMutableExtras, T> {
    konst factory: () -> T

    override fun getValue(thisRef: HasMutableExtras, property: KProperty<*>): T {
        return thisRef.extras.getOrPut(key, factory)
    }

    override fun setValue(thisRef: HasMutableExtras, property: KProperty<*>, konstue: T) {
        thisRef.extras[key] = konstue
    }
}

interface ExtrasLazyProperty<Receiver : HasMutableExtras, T : Any> : ExtrasProperty<T>, ReadWriteProperty<Receiver, T> {
    konst factory: Receiver.() -> T

    override fun getValue(thisRef: Receiver, property: KProperty<*>): T {
        return thisRef.extras.getOrPut(key) { thisRef.factory() }
    }

    override fun setValue(thisRef: Receiver, property: KProperty<*>, konstue: T) {
        thisRef.extras[key] = konstue
    }
}

interface NullableExtrasLazyProperty<Receiver : HasMutableExtras, T : Any> : ExtrasProperty<Optional<T>>, ReadOnlyProperty<Receiver, T?> {
    konst factory: Receiver.() -> T?

    override fun getValue(thisRef: Receiver, property: KProperty<*>): T? {
        return thisRef.extras.getOrPut(key) { Optional.ofNullable(thisRef.factory()) }.let { if (it.isPresent) it.get() else null }
    }
}
