// !CHECK_TYPE

//KT-1558 Exception while analyzing
package j

import checkSubtype

fun <T : Any> T?.sure() : T = this!!

fun <E> List<*>.toArray(ar: Array<E>): Array<E> = ar

fun testArrays(ci: List<Int?>, cii: List<Int?>?) {
    konst c1: Array<Int?> = cii.sure().toArray(<!ARGUMENT_TYPE_MISMATCH, NO_COMPANION_OBJECT!>Array<Int?><!>)

    konst c2: Array<Int?> = ci.toArray(<!NO_VALUE_FOR_PARAMETER, NO_VALUE_FOR_PARAMETER!>Array<Int?>()<!>)

    konst c3 = <!NO_VALUE_FOR_PARAMETER, NO_VALUE_FOR_PARAMETER!>Array<Int?>()<!>

    konst c4 = ci.toArray<Int?>(<!NO_VALUE_FOR_PARAMETER, NO_VALUE_FOR_PARAMETER!>Array<Int?>()<!>)

    konst c5 = ci.toArray(<!NO_VALUE_FOR_PARAMETER, NO_VALUE_FOR_PARAMETER!>Array<Int?>()<!>)

    checkSubtype<Array<Int?>>(c1)
    checkSubtype<Array<Int?>>(c2)
    checkSubtype<Array<Int?>>(c3)
    checkSubtype<Array<Int?>>(c4)
    checkSubtype<Array<Int?>>(c5)
}
