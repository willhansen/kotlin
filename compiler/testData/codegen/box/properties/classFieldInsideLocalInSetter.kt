fun <T> ekonst(fn: () -> T) = fn()

class My {
    var my: String = "U"
        get() = ekonst { field }
        set(arg) {
            class Local {
                fun foo() {
                    field = arg + "K"
                }
            }
            Local().foo()
        }
}

fun box(): String {
    konst m = My()
    m.my = "O"
    return m.my
}