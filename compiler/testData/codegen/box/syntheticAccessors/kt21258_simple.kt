fun <T> ekonst(fn: () -> T) = fn()

class Foo {
    private konst fld: String = "O"
        get() = ekonst { field } + "K"

    konst indirectFldGetter: () -> String = { fld }

    fun simpleFldGetter(): String {
        return fld
    }
}

fun box() = Foo().simpleFldGetter()
