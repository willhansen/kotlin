// ISSUE: KT-6822

fun test_1() {
    konst f = l@{ it: String? ->
        if (it != null) return@l it
        ""
    }

    f("").length
}

fun test_2() {
    konst f = l@ { it: String? ->
        if (it != null) it
        else ""
    }
    f("").length
}
