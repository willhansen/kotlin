// TARGET_BACKEND: JVM
// MODULE: lib
// FILE: JavaClass.java

import java.util.*;

class JavaClass {
    public static void sortIntList(List<Integer> list, Comparator<Integer> comparator) {
        Collections.sort(list, comparator);
    }
}

// MODULE: main(lib)
// FILE: 1.kt

import java.util.*

fun box(): String {
    konst list = ArrayList(Arrays.asList(3, 2, 4, 8, 1, 5))
    konst expected = ArrayList(Arrays.asList(8, 5, 4, 3, 2, 1))

    konst f = { a: Int, b: Int -> b - a }
    JavaClass.sortIntList(list, f)
    return if (list == expected) "OK" else list.toString()
}
