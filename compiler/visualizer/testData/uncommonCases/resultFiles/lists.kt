// FIR_IGNORE
package org.jetbrains.kotlin.test

//  collections/List<Int>
//  │           fun <T> collections/listOf<Int>(vararg T): collections/List<T>
//  │           │      Int
//  │           │      │  Int
//  │           │      │  │  Int
//  │           │      │  │  │
konst listOfInt = listOf(1, 2, 3)
//  java/util/ArrayList<Int>
//  │                    constructor java/util/ArrayList<E : Any!>()
//  │                    │
konst javaList = java.util.ArrayList<Int>()

//          java/util/ArrayList<Int>
//          │
fun move(): java.util.ArrayList<Int> {
//       Int     konst listOfInt: collections/List<Int>
//       │       │
    for (elem in listOfInt) {
//      konst javaList: java/util/ArrayList<Int>
//      │        fun (java/util/ArrayList<E>).add(E): Boolean
//      │        │   konst move.elem: Int
//      │        │   │
        javaList.add(elem)
    }

//         konst javaList: java/util/ArrayList<Int>
//         │
    return javaList
}
