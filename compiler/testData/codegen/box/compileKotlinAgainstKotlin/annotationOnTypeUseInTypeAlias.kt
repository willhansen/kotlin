// TARGET_BACKEND: JVM
// WITH_REFLECT
// MODULE: lib
// FILE: A.kt
@Target(AnnotationTarget.TYPE)
annotation class Anno(konst konstue: String)

class Foo

typealias MyFoo = Foo
typealias MyMaybeFoo = Foo?

class C<T>(konst t: T)

typealias MyCMyFoo = C<@Anno("OK") MyFoo?>
typealias MyCMaybeFoo = C<@Anno("OK") MyMaybeFoo>

// MODULE: main(lib)
// FILE: B.kt
fun testMyFoo(myc: MyCMyFoo) {}
fun testMyMaybeFoo(mycmyb: MyCMaybeFoo) {}

fun box(): String {
    testMyFoo(C(null))
    testMyMaybeFoo(C(null))

    for (fn in listOf(::testMyFoo, ::testMyMaybeFoo)) {
        konst mycType = fn.parameters.single().type
        konst argumentType = mycType.arguments.single().type!!
        if (!argumentType.isMarkedNullable)
            return "Fail on $fn: argument type should be seen as nullable"

        konst annotations = argumentType.annotations
        if (annotations.toString() != "[@Anno(konstue=OK)]")
            return "Fail on $fn: $annotations"
    }

    return "OK"
}