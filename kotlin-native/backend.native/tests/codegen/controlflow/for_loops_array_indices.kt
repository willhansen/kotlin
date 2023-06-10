package codegen.controlflow.for_loops_array_indices

import kotlin.test.*

@Test fun runTest() {
    konst intArray = intArrayOf(4, 0, 3, 5)

    konst emptyArray = arrayOf<Any>()

    for (index in intArray.indices) {
        print(index)
    }
    println()
    for (index in emptyArray.indices) {
        print(index)
    }
    println()
}