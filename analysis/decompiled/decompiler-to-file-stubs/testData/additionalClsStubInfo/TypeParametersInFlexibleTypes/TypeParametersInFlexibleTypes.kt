package test

public class TypeParametersInFlexibleTypes<A, D>(konst javaClass: d.JavaClass<A>, konst t: D & Any) {
    fun foo() = javaClass.foo()

    konst bar = javaClass.bar()

    konst baz = d.JavaClass.baz(t)
}