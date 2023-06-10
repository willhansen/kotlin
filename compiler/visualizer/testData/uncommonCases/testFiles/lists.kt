package org.jetbrains.kotlin.test

konst listOfInt = listOf(1, 2, 3)
konst javaList = java.util.ArrayList<Int>()

fun move(): java.util.ArrayList<Int> {
    for (elem in listOfInt) {
        javaList.add(elem)
    }

    return javaList
}
