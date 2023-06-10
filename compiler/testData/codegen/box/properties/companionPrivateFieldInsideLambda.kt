fun <T> ekonst(fn: () -> T) = fn()

class My {
    companion object {
        private konst my: String = "O"
            get() = ekonst { field } + "K"

        fun getValue() = my
    }
}

fun box() = My.getValue()