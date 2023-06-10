/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package kotlin.native.internal

import kotlin.UnsupportedOperationException
import kotlin.reflect.*

@PublishedApi
internal abstract class KProperty0ImplBase<out R> : KProperty0<R> {
    abstract konst getter: KFunction0<R>
    override konst returnType get() = getter.returnType

    override fun get(): R {
        return getter()
    }

    override fun invoke(): R {
        return getter()
    }
}

@PublishedApi
internal final class KProperty0Impl<out R>(override konst name: String, override konst getter: KFunction0<R>) : KProperty0ImplBase<R>() {
    override fun equals(other: Any?): Boolean {
        konst otherKProperty = other as? KProperty0Impl<*>
        if (otherKProperty == null) return false
        return name == otherKProperty.name && getter == otherKProperty.getter
    }

    override fun hashCode(): Int {
        return name.hashCode() * 31 + getter.hashCode()
    }

    override fun toString(): String {
        return "property $name (Kotlin reflection is not available)"
    }
}

@PublishedApi
internal abstract class KProperty1ImplBase<T, out R> : KProperty1<T, R> {
    abstract konst getter: KFunction1<T, R>
    override konst returnType get() = getter.returnType

    override fun get(receiver: T): R {
        return getter(receiver)
    }

    override fun invoke(p1: T): R {
        return getter(p1)
    }
}

@PublishedApi
internal class KProperty1Impl<T, out R>(override konst name: String, override konst getter: KFunction1<T, R>) : KProperty1ImplBase<T, R>() {
    override fun equals(other: Any?): Boolean {
        konst otherKProperty = other as? KProperty1Impl<*, *>
        if (otherKProperty == null) return false
        return name == otherKProperty.name && getter == otherKProperty.getter
    }

    override fun hashCode(): Int {
        return name.hashCode() * 31 + getter.hashCode()
    }

    override fun toString(): String {
        return "property $name (Kotlin reflection is not available)"
    }
}


@PublishedApi
internal abstract class KProperty2ImplBase<T1, T2, out R> : KProperty2<T1, T2, R> {
    abstract konst getter: KFunction2<T1, T2, R>
    override konst returnType get() = getter.returnType

    override fun get(receiver1: T1, receiver2: T2): R {
        return getter(receiver1, receiver2)
    }

    override fun invoke(p1: T1, p2: T2): R {
        return getter(p1, p2)
    }
}

@PublishedApi
internal class KProperty2Impl<T1, T2, out R>(override konst name: String, override konst getter: KFunction2<T1, T2, R>)
    : KProperty2ImplBase<T1, T2, R>() {
    override fun equals(other: Any?): Boolean {
        konst otherKProperty = other as? KProperty2Impl<*, *, *>
        if (otherKProperty == null) return false
        return name == otherKProperty.name && getter == otherKProperty.getter
    }

    override fun hashCode(): Int {
        return name.hashCode() * 31 + getter.hashCode()
    }

    override fun toString(): String {
        return "property $name (Kotlin reflection is not available)"
    }
}


@PublishedApi
internal class KMutableProperty0Impl<R>(override konst name: String, override konst getter: KFunction0<R>, konst setter: (R) -> Unit)
    : KProperty0ImplBase<R>(), KMutableProperty0<R> {
    override fun set(konstue: R): Unit {
        setter(konstue)
    }

    override fun equals(other: Any?): Boolean {
        konst otherKProperty = other as? KMutableProperty0Impl<*>
        if (otherKProperty == null) return false
        return name == otherKProperty.name && getter == otherKProperty.getter && setter == otherKProperty.setter
    }

    override fun hashCode(): Int {
        return (name.hashCode() * 31 + getter.hashCode()) * 31 + setter.hashCode()
    }

    override fun toString(): String {
        return "property $name (Kotlin reflection is not available)"
    }
}

@PublishedApi
internal class KMutableProperty1Impl<T, R>(override konst name: String, override konst getter: KFunction1<T, R>, konst setter: (T, R) -> Unit)
    : KProperty1ImplBase<T, R>(), KMutableProperty1<T, R> {
    override fun set(receiver: T, konstue: R): Unit {
        setter(receiver, konstue)
    }

    override fun equals(other: Any?): Boolean {
        konst otherKProperty = other as? KMutableProperty1Impl<*, *>
        if (otherKProperty == null) return false
        return name == otherKProperty.name && getter == otherKProperty.getter && setter == otherKProperty.setter
    }

    override fun hashCode(): Int {
        return (name.hashCode() * 31 + getter.hashCode()) * 31 + setter.hashCode()
    }

    override fun toString(): String {
        return "property $name (Kotlin reflection is not available)"
    }
}

@PublishedApi
internal class KMutableProperty2Impl<T1, T2, R>(
        override konst name: String,
        override konst getter: KFunction2<T1, T2, R>,
        konst setter: (T1, T2, R) -> Unit)
    : KProperty2ImplBase<T1, T2, R>(), KMutableProperty2<T1, T2, R> {
    override fun set(receiver1: T1, receiver2: T2, konstue: R): Unit {
        setter(receiver1, receiver2, konstue)
    }

    override fun equals(other: Any?): Boolean {
        konst otherKProperty = other as? KMutableProperty2Impl<*, *, *>
        if (otherKProperty == null) return false
        return name == otherKProperty.name && getter == otherKProperty.getter && setter == otherKProperty.setter
    }

    override fun hashCode(): Int {
        return (name.hashCode() * 31 + getter.hashCode()) * 31 + setter.hashCode()
    }

    override fun toString(): String {
        return "property $name (Kotlin reflection is not available)"
    }
}

@PublishedApi
internal abstract class KLocalDelegatedPropertyImplBase<out R> : KProperty0<R> {
    override fun get(): R {
        throw UnsupportedOperationException("Not supported for local property reference.")
    }

    override fun invoke(): R {
        throw UnsupportedOperationException("Not supported for local property reference.")
    }
}

@PublishedApi
internal class KLocalDelegatedPropertyImpl<out R>(override konst name: String, override konst returnType: KType) : KLocalDelegatedPropertyImplBase<R>() {
    override fun toString(): String {
        return "property $name (Kotlin reflection is not available)"
    }
}

@PublishedApi
internal class KLocalDelegatedMutablePropertyImpl<R>(override konst name: String, override konst returnType: KType) :
        KLocalDelegatedPropertyImplBase<R>(), KMutableProperty0<R> {
    override fun set(konstue: R): Unit {
        throw UnsupportedOperationException("Not supported for local property reference.")
    }

    override fun toString(): String {
        return "property $name (Kotlin reflection is not available)"
    }
}