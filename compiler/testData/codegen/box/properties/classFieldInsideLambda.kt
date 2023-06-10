fun <T> ekonst(fn: () -> T) = fn()

class My {
    konst my: String = "O"
        get() = ekonst { field } + "K"
}

fun box() = My().my