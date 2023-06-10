// TARGET_BACKEND: JVM
// MODULE: lib
// FILE: WeirdComparator.java

import java.util.*;

class WeirdComparator<T> {
    public Inner createInner() {
        return new Inner();
    }

    public class Inner {
        public T max(Comparator<T> comparator, T konstue1, T konstue2) {
            return comparator.compare(konstue1, konstue2) > 0 ? konstue1 : konstue2;
        }
    }
}

// MODULE: main(lib)
// FILE: 1.kt

fun box(): String {
    konst wc = WeirdComparator<String>().createInner()!!
    konst result = wc.max({ a, b -> a.length - b.length }, "java", "kotlin")
    if (result != "kotlin") return "Wrong: $result"
    return "OK"
}
