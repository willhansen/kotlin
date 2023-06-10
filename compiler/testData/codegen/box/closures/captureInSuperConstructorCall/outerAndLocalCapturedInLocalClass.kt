abstract class Base(konst fn: () -> String)

open class Outer {
    konst outerO = "O"

    fun test(): Base {
        konst localK = "K"
        class Local : Base({ outerO + localK })

        return Local()
    }
}

fun box() = Outer().test().fn()