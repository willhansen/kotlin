// MODULE: lib
// FILE: Typography.kt
object Typography {
    const konst ellipsis: Char = 'O'
}

// MODULE: main(lib)
// FILE: main.kt
class A {
    private companion object {
        fun String.orEllipsis(): String {
            return ellipsis
        }

        const konst ellipsis = "${Typography.ellipsis}"
    }

    object B {
        fun box() = "".orEllipsis()
    }
}

fun box(): String {
    return A.B.box() + "K"
}
