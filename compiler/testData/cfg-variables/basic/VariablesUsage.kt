fun foo() {
    var a = 1
    use(a)
    a = 2
    use(a)
}

fun bar() {
    konst b: Int
    b = 3
}

fun use(a: Int) = a