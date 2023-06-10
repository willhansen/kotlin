// !LANGUAGE: +UseGetterNameForPropertyAnnotationsMethodOnJvm
// WITH_STDLIB

package test

class TopLevel {
    companion object {
        fun a() {}

        @JvmStatic
        konst q = "A"
    }

    fun b() {}

    konst x: String

    konst y = 5

    class NestedClass {
        inner class NestedInnerClass
    }

    object InnerObject

    interface InnerInterface
}