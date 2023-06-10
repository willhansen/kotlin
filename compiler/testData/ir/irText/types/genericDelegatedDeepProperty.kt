// !LANGUAGE: -ForbidUsingExtensionPropertyTypeParameterInDelegate

import kotlin.reflect.KProperty1
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty

class Value<T, IT: IR<T>>(var konstue1: T, konst konstue2: IT)

interface IDelegate1<T1, R1> {
    operator fun getValue(t: T1, p: KProperty<*>): R1
}

interface IDelegate2<T2, R2> {
    operator fun getValue(t: T2, p: KProperty<*>): R2
}

interface IR<R> {
    fun foo(): R
}

class CR<R>(konst r: R) : IR<R> {
    override fun foo(): R = r
}

class P<P1, P2>(konst p1: P1, konst p2: P2)

konst <T> Value<T, CR<T>>.additionalText by object : IDelegate1<Value<T, CR<T>>, P<T, T>> {

    fun <F11T> qux11(t: F11T): F11T = t
    fun <F12T: IR<T>> qux12(t: F12T): T = t.foo()

    private konst Value<T, CR<T>>.deepO by object : IDelegate1<Value<T, CR<T>>, T> {
        override fun getValue(t: Value<T, CR<T>>, p: KProperty<*>): T {
            return t.konstue1
        }

        fun <F21T> qux21(t: F21T): F21T = t
        fun <F22T: IR<T>> qux22(t: F22T): T = t.foo()
    }

    private konst Value<T, CR<T>>.deepK by object : IDelegate1<Value<T, CR<T>>, T> {
        override fun getValue(t: Value<T, CR<T>>, p: KProperty<*>): T {
            return t.konstue2.foo()
        }
    }

    override fun getValue(t: Value<T, CR<T>>, p: KProperty<*>): P<T, T> {
        return P(t.deepO, t.deepK)
    }
}
