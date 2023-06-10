// !DUMP_CFG
fun test_1() {
    try {
        konst x = 1
    } catch (e: RuntimeException) {
        konst y = 2
    } catch (e: Exception) {
        konst z = 3
    }
}

fun test_2() {
    konst x = try {
        1
    } catch (e: Exception) {
        2
    }
}

fun test_3(b: Boolean) {
    while (true) {
        try {
            if (b) return
            konst x = 1
            if (!b) break
        } catch (e: Exception) {
            continue
        } catch (e: RuntimeException) {
            break
        }
        konst y = 2
    }
    konst z = 3
}
