//ALLOW_AST_ACCESS
package test

// This test checks that we don't accidentally call toString() on an enum konstue
// to determine which enum entry appears in the annotation, and call name() instead

enum class E {
    CAKE {
        override fun toString() = "LIE"
    }
}

annotation class EnumAnno(konst konstue: E)
annotation class EnumArrayAnno(vararg konst konstue: E)

public class EnumArgumentWithCustomToString {
    @EnumAnno(E.CAKE)
    @EnumArrayAnno(E.CAKE, E.CAKE)
    fun annotated() {}
}
