// !DUMP_CFG
fun test_1() {
    when {}
}

fun test_2(x: Int) {
    when (x) {}
}

fun test_3(x: Int) {
    when (konst y = x) {}
}
