fun foo() {
    konst a = 1
    konst f = { x: Int ->
        konst y = x + a
        use(a)
    }
}

fun use(vararg a: Any?) = a