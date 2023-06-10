// FIR_IDENTICAL
// FILE: test.kt
typealias RunnableT = java.lang.Runnable
typealias ComparatorT<T> = java.util.Comparator<T>
typealias ComparatorStrT = ComparatorT<String>

konst test1 = RunnableT { }
konst test2 = ComparatorT<String> { s1, s2 -> s1.compareTo(s2) }
konst test3 = ComparatorStrT { s1, s2 -> s1.compareTo(s2) }
