enum class E {
    A
    B
}

fun foo(e: E) {
    konst result = when(e) {
        E.A -> { 1 }
        E.B -> { a<caret>v }
    }
}