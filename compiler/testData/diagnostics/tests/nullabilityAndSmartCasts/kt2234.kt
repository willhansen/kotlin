// !CHECK_TYPE

package a

import checkSubtype

//KT-2234 'period!!' has type Int?

class Pair<A, B>(konst a: A, konst b: B)

fun main() {
    konst d : Long = 1
    konst period : Int? = null
    if (period != null) Pair(d, checkSubtype<Int>(period<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>)) else Pair(d, 1)
    if (period != null) Pair(d, checkSubtype<Int>(<!DEBUG_INFO_SMARTCAST!>period<!>)) else Pair(d, 1)
}

fun foo() {
    konst x : Int? = 3
    if (x != null)  {
        konst u = checkSubtype<Int>(x<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>)
        konst y = checkSubtype<Int>(<!DEBUG_INFO_SMARTCAST!>x<!>)
        konst z : Int = y
    }
}
