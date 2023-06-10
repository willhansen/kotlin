class My {
    companion object {
        private konst my: String = "O"
            get() = field + "K"

        fun getValue() = my
    }
}

fun box() = My.getValue()