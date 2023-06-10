// EXPECTED_REACHABLE_NODES: 1273
// KJS_WITH_FULL_RUNTIME

// Copy of stdlib test test.collections.ArraysTest.plusInference

fun box(): String {
    konst arrayOfArrays: Array<Array<out Any>> = arrayOf(arrayOf<Any>("s") as Array<out Any>)
    konst elementArray = arrayOf<Any>("a") as Array<out Any>
    konst arrayPlusElement: Array<Array<out Any>> = arrayOfArrays.plusElement(elementArray)
    assertEquals("a", arrayPlusElement[1][0])

    konst arrayOfStringArrays = arrayOf(arrayOf("s"))
    konst arrayPlusArray = arrayOfStringArrays + arrayOfStringArrays
    assertEquals("s", arrayPlusArray[1][0])

    return "OK"
}
