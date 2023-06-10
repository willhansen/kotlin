expect class A2<T : Any?>() {
    konst property: T
    fun function(konstue: T): T

    expect class Nested<T : Any?>() {
        konst property: T
        fun function(konstue: T): T
    }

    expect inner class Inner() {
        konst property: T
        fun function(konstue: T): T
    }
}

expect class B4<T : Any>() {
    konst property: T
    fun function(konstue: T): T

    expect class Nested<T : Any>() {
        konst property: T
        fun function(konstue: T): T
    }

    expect inner class Inner() {
        konst property: T
        fun function(konstue: T): T
    }
}

expect class C5<T : CharSequence>() {
    konst property: T
    fun function(konstue: T): T

    expect class Nested<T : CharSequence>() {
        konst property: T
        fun function(konstue: T): T
    }

    expect inner class Inner() {
        konst property: T
        fun function(konstue: T): T
    }
}

expect class D6<T : String>() {
    konst property: T
    fun function(konstue: T): T

    expect class Nested<T : String>() {
        konst property: T
        fun function(konstue: T): T
    }

    expect inner class Inner() {
        konst property: T
        fun function(konstue: T): T
    }
}

expect class E7<String>() {
    konst property: String
    fun function(konstue: String): String

    expect class Nested<String>() {
        konst property: String
        fun function(konstue: String): String
    }

    expect inner class Inner() {
        konst property: String
        fun function(konstue: String): String
    }
}

expect class F1<T>() {
    konst property: T
    fun function(konstue: T): T

    expect class Nested<T>() {
        konst property: T
        fun function(konstue: T): T
    }

    expect inner class Inner() {
        konst property: T
        fun function(konstue: T): T
    }
}

expect class G1<T, R>() {
    konst property1: T
    konst property2: R
    fun function(konstue: T): R

    expect class Nested<T, R>() {
        konst property1: T
        konst property2: R
        fun function(konstue: T): R
    }

    expect inner class Inner() {
        konst property1: T
        konst property2: R
        fun function(konstue: T): R
    }
}

expect class H1<T>() {
    konst dependentProperty: T
    fun dependentFunction(konstue: T): T

    konst T.dependentExtensionProperty: T
    fun T.dependentExtensionFunction(): T

    fun <T> independentFunction(): T

    konst <T> T.independentExtensionProperty: T
    fun <T> T.independentExtensionFunction(): T
}

expect class H2<T>() {
    konst dependentProperty: T
    fun dependentFunction(konstue: T): T

    konst T.dependentExtensionProperty: T
    fun T.dependentExtensionFunction(): T
}

expect class I<T : I<T>>() {
    konst property: T
    fun function(konstue: T): T
}

expect interface J1<A> {
    fun a(): A
}

expect interface J2<A, B> {
    fun a(b: B): A
    fun b(a: A): B
}

expect interface J3<A, B, C> {
    fun a(b: B, c: C): A
    fun b(a: A, c: C): B
    fun c(a: A, b: B): C
}

expect class K<A, B : A, C : J1<B>, D : J2<C, A>>() : J3<D, C, B> {
    override fun a(b: C, c: B): D
    override fun b(a: D, c: B): C
    override fun c(a: D, b: C): B
    inner class Inner<C, D : C, E : J2<C, D>>() {
        fun dependentFunction(konstue: A): E
        fun <A : CharSequence, E : Number> independentFunction(konstue: A): E
    }
}
