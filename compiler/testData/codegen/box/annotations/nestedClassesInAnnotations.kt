// !LANGUAGE: +NestedClassesInAnnotations

annotation class Foo(konst kind: Kind) {
    enum class Kind { FAIL, OK }
}

@Foo(Foo.Kind.OK)
fun box(): String {
    return Foo.Kind.OK.name
}
