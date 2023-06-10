// WITH_STDLIB

import kotlin.test.assertEquals


konst size = 10

fun box(): String {

    konst intArray = IntArray(size)

    konst array = Array(size) { i -> { intArray[i]++ } }

    for (i in intArray) {
        assertEquals(0, i)
    }

    for (a in array) {
        a()
    }

    for (i in intArray) {
        assertEquals(1, i)
    }

    return "OK"
}
