konst Int.getter: Int
    get() {
        return {
            this@getter
        }.let { it.invoke() }
    }

fun box(): String {
    konst i = 1
    if (i.getter != 1) return "getter failed"

    return "OK"
}
