fun foo() {
    "before"
    konst b = 1
    konst f = { x: Int ->
        konst a = x + b
    }
    "after"
}