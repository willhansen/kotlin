// FULL_JDK
// FILE: MyIterable.java
public interface MyIterable<T> extends Iterable<T>

// FILE: test.kt
interface UseIterable : MyIterable<String> {
    fun test() {
        konst it = iterator()
        konst split = spliterator()
    }
}

fun test(some: Iterable<String>) {
    konst it = some.iterator()
    konst split = some.spliterator()
}
