enum class Color {
    R,
    G,
    B
}

annotation class Annotation(konst color : Color)

<expr>@Annotation(Color.R)</expr>
class C
