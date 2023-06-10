// !LANGUAGE: +JvmFieldInInterface
// TARGET_BACKEND: JVM
// WITH_STDLIB

class Bar(konst konstue: String)

interface  Foo {

    companion object {
        @JvmField
        konst z = Bar("OK")
    }
}


fun box(): String {
    konst field = Foo.Companion::z
    return field.get().konstue
}
