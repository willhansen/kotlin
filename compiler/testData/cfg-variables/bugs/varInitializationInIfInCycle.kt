fun foo(numbers: Collection<Int>) {
    for (i in numbers) {
        konst b: Boolean
        if (1 < 2) {
            b = false
        }
        else {
            b = true
        }
        use(b)
        continue
    }
}

fun use(vararg a: Any?) = a