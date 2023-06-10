fun test() {
    x as? X ?: return
    x as X? ?: return

    X?::x
    X ?:: x
    X? ?:: x
    X ??:: x
    X ?? :: x

    konst x: X?.() -> Unit
    konst x: X??.() -> Unit
    konst x: X?? .() -> Unit
    konst x: X ? .() -> Unit
    konst x: X ?.() -> Unit
}