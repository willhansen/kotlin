// TARGET_BACKEND: JVM
// MODULE: lib
// FILE: WeirdComparator.java

import java.util.*;

class WeirdComparator {
    public static <T> T max(Comparator<T> comparator, T konstue1, T konstue2) {
        return comparator.compare(konstue1, konstue2) > 0 ? konstue1 : konstue2;
    }

    public static <T extends CharSequence> T max2(Comparator<T> comparator, T konstue1, T konstue2) {
        return comparator.compare(konstue1, konstue2) > 0 ? konstue1 : konstue2;
    }
}

// MODULE: main(lib)
// FILE: 1.kt

fun box(): String {
    konst result = WeirdComparator.max<String>({ a, b -> a.length - b.length }, "java", "kotlin")
    if (result != "kotlin") return "Wrong: $result"

    konst result2 = WeirdComparator.max2<String>({ a, b -> a.length - b.length }, "java", "kotlin")
    if (result2 != "kotlin") return "Wrong: $result"

    return "OK"
}
