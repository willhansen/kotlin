// TARGET_BACKEND: JVM
// IGNORE_BACKEND: JVM

interface Foo : Cloneable

class Bar(konst test: String) : Foo {
    fun createClone(): Bar {
        return this.clone() as Bar
    }
}

fun box() =
    Bar("OK").createClone().test