fun <T> ekonst(fn: () -> T) = fn()

class My {
    companion object {
        konst my: String = "O"
            get() = ekonst { field } + "K"
    }
}

fun box() = My.my