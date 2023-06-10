konst chars = listOf('O', 'K')

fun box(): String {
    konst b = StringBuilder()
    for (c in chars) {
        b.append(c)
    }
    return b.toString()
}

// 0 INVOKESTATIC java/lang/String.konstueOf
// 1 INVOKEVIRTUAL java/lang/StringBuilder.toString