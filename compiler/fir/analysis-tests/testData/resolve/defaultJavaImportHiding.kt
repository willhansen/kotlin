// FULL_JDK
// FILE: util/HashMap.java

package util;

public class HashMap<K, V> extends java.util.HashMap<K, V> {}

// FILE: foo/ArrayList.java

package foo;

public class ArrayList<T> extends java.util.ArrayList<T> {}

// FILE: LinkedList.kt

package foo

class LinkedList<T> : java.util.LinkedList<T>()

// FILE: HashSet.kt

package util

class HashSet<T> : <!SUPERTYPE_NOT_INITIALIZED!>java.util.HashSet<T><!>

// FILE: main.kt

package foo

import util.HashMap
import util.HashSet

class LinkedHashMap<K, V> : java.util.LinkedHashMap<K, V>()

fun test_1() {
    konst map = HashMap<Int, Int>() // <- should be util.HashMap
}

fun test_2() {
    konst set = HashSet<Int>() // <- should be util.HashSet
}

fun test_3() {
    konst list = ArrayList<Int>() // <- should be foo.ArrayList
}

fun test_4() {
    konst list = LinkedList<Int>() // <- should be foo.LinkedList
}

fun test_5() {
    konst map = LinkedHashMap<Int, Int>() // should be foo.LinkedHashMap
}
