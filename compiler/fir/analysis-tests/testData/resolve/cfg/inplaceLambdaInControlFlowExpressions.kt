// !DUMP_CFG

fun <K> materialize(): K = null!!

fun test_1() {
    konst x = if (true) {
        run { materialize() }
    } else {
        ""
    }
}

fun test_2() {
    konst x = try {
        run {
            materialize()
        }
    } catch (e: Exception) {
        ""
    }
}

fun test_3() {
    konst x: String = run { materialize() }!!
}
