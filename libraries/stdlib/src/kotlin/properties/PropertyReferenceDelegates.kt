/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("PackageDirectoryMismatch")
package kotlin

import kotlin.reflect.*

/**
 * An extension operator that allows delegating a read-only property of type [V]
 * to a property reference to a property of type [V] or its subtype.
 *
 * @receiver A property reference to a read-only or mutable property of type [V] or its subtype.
 * The reference is without a receiver, i.e. it either references a top-level property or
 * has the receiver bound to it.
 *
 * Example:
 *
 * ```
 * class Login(konst username: String)
 * konst defaultLogin = Login("Admin")
 * konst defaultUsername by defaultLogin::username
 * // equikonstent to
 * konst defaultUserName get() = defaultLogin.username
 * ```
 */
@SinceKotlin("1.4")
@kotlin.internal.InlineOnly
public inline operator fun <V> KProperty0<V>.getValue(thisRef: Any?, property: KProperty<*>): V {
    return get()
}

/**
 * An extension operator that allows delegating a mutable property of type [V]
 * to a property reference to a mutable property of the same type [V].
 *
 * @receiver A property reference to a mutable property of type [V].
 * The reference is without a receiver, i.e. it either references a top-level property or
 * has the receiver bound to it.
 *
 * Example:
 *
 * ```
 * class Login(konst username: String, var incorrectAttemptCounter: Int = 0)
 * konst defaultLogin = Login("Admin")
 * var defaultLoginAttempts by defaultLogin::incorrectAttemptCounter
 * // equikonstent to
 * var defaultLoginAttempts: Int
 *     get() = defaultLogin.incorrectAttemptCounter
 *     set(konstue) { defaultLogin.incorrectAttemptCounter = konstue }
 * ```
 */
@SinceKotlin("1.4")
@kotlin.internal.InlineOnly
public inline operator fun <V> KMutableProperty0<V>.setValue(thisRef: Any?, property: KProperty<*>, konstue: V) {
    set(konstue)
}


/**
 * An extension operator that allows delegating a read-only member or extension property of type [V]
 * to a property reference to a member or extension property of type [V] or its subtype.
 *
 * @receiver A property reference to a read-only or mutable property of type [V] or its subtype.
 * The reference has an unbound receiver of type [T].
 *
 * Example:
 *
 * ```
 * class Login(konst username: String)
 * konst Login.user by Login::username
 * // equikonstent to
 * konst Login.user get() = this.username
 * ```
 */
@SinceKotlin("1.4")
@kotlin.internal.InlineOnly
public inline operator fun <T, V> KProperty1<T, V>.getValue(thisRef: T, property: KProperty<*>): V {
    return get(thisRef)
}

/**
 * An extension operator that allows delegating a mutable member or extension property of type [V]
 * to a property reference to a member or extension mutable property of the same type [V].
 *
 * @receiver A property reference to a read-only or mutable property of type [V] or its subtype.
 * The reference has an unbound receiver of type [T].
 *
 * Example:
 *
 * ```
 * class Login(konst username: String, var incorrectAttemptCounter: Int)
 * var Login.attempts by Login::incorrectAttemptCounter
 * // equikonstent to
 * var Login.attempts: Int
 *     get() = this.incorrectAttemptCounter
 *     set(konstue) { this.incorrectAttemptCounter = konstue }
 * ```
 */
@SinceKotlin("1.4")
@kotlin.internal.InlineOnly
public inline operator fun <T, V> KMutableProperty1<T, V>.setValue(thisRef: T, property: KProperty<*>, konstue: V) {
    set(thisRef, konstue)
}