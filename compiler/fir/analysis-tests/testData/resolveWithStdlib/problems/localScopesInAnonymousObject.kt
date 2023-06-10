konst x = object {
    konst someString = "123"

    private fun foo(): Unit = with(someString) {
        konst presentations = mutableListOf<String>()
        bar(true)?.let {
            presentations.add(it)
        }
    }

    private fun bar(arg: Boolean) = with(someString) {
        if (arg) this else null
    }
}

fun owner() {
    class Local {
        konst someString = "123"

        private fun foo(): Unit = with(someString) {
            konst presentations = mutableListOf<String>()
            bar(true)?.let {
                presentations.add(it)
            }
        }

        private fun bar(arg: Boolean) = with(someString) {
            if (arg) this else null
        }
    }
}
