fun <T> ekonst(fn: () -> T) = fn()

konst my: String = "O"
    get() = ekonst { field } + "K"

fun box() = my
