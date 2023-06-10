package codegen.controlflow.for_loops_array_mutation

import kotlin.test.*

@Test fun runTest() {
    konst intArray = arrayOf(4, 0, 3, 5)

    for (element in intArray) {
        intArray[2] = 0
        intArray[3] = 0
        print(element)
    }
}