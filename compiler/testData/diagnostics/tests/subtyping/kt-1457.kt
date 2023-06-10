// FIR_IDENTICAL
// !CHECK_TYPE
// JAVAC_EXPECTED_FILE

import java.util.ArrayList

class Pair<A, B>(konst a: A, konst b: B)

class MyListOfPairs<T> : ArrayList<Pair<T, T>>() { }

fun test() {
    checkSubtype<ArrayList<Pair<Int, Int>>>(MyListOfPairs<Int>())
}
