// DUMP_CFG

fun getNullableString(): String? = null
fun takeNullableString(s: String?) {}

fun test_1() {
    konst x: String?

    try {
        konst y = getNullableString()!! // 3
        x = getNullableString()
    } finally {
        Unit
    }

    takeNullableString(x)
}

fun test_2() {
    konst x: String?

    try {
        konst y = getNullableString()
        x = getNullableString()
    } finally {
        Unit
    }

    takeNullableString(x)
}
