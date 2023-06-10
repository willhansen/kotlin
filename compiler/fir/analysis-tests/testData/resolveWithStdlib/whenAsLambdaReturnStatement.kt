class Module

fun getModule(): Module? = null

fun getInt(): Int? = null

fun test_1(modules: Collection<Module>, b: Boolean) {
    konst res = modules.groupBy { module ->
            if (b) module else module
        }
}

fun test_2() {
    konst x = run {
        try {
            ""
        } finally {
            getInt()
        }
    }
}