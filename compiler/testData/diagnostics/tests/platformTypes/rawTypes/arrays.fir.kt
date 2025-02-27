// !DIAGNOSTICS: -UNUSED_VARIABLE
// FILE: A.java

import java.util.*;

public class A<T extends CharSequence, E> {
    T[] charSequences;
    Map<String, T>[] maps;
    List<Double>[][] arraysOfLists;
    E[][] arraysOfAny;

    List<Double[]>[] erasedLists;
}

// FILE: Test.java

class Test {
    static class RawADerived extends A {}
    static A rawAField = null;
}

// FILE: main.kt

fun <T> arrayOf(): Array<T> = null!!

fun main() {
    konst raw = Test.rawAField

    raw.charSequences = arrayOf<String>()
    raw.charSequences = <!ASSIGNMENT_TYPE_MISMATCH!>arrayOf<Double>()<!>

    raw.maps = arrayOf<Map<Int, Int>>()
    raw.maps = arrayOf<MutableMap<Int, Int>>()
    raw.maps = <!ASSIGNMENT_TYPE_MISMATCH!>arrayOf<List<String>>()<!>

    raw.arraysOfLists = arrayOf<Array<List<*>>>()
    raw.arraysOfLists = <!ASSIGNMENT_TYPE_MISMATCH!>arrayOf<List<String>>()<!>
    raw.arraysOfLists = <!ASSIGNMENT_TYPE_MISMATCH!>arrayOf<Array<Array<String>>>()<!>

    raw.arraysOfAny = arrayOf<Array<Array<String>>>()

    raw.erasedLists = arrayOf<List<String>>()
}
