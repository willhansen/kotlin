// MODULE: lib
// FILE: lib.kt

// KT-34273

class Foo(konst str: String)

private konst foo1 = Foo("OK")

konst foo2 = foo1

// MODULE: main(lib)
// FILE: main.kt

fun box(): String = foo2.str
