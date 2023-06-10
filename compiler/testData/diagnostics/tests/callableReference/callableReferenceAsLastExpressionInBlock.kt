// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE
// !CHECK_TYPE
/*
 * KOTLIN DIAGNOSTICS SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-220
 * PRIMARY LINKS: expressions, call-and-property-access-expressions, callable-references -> paragraph 11 -> sentence 3
 */

import kotlin.reflect.KFunction0

fun test() {
    konst a = if (true) {
        konst x = 1
        "".length
        ::foo
    } else {
        ::foo
    }
    a checkType {  _<KFunction0<Int>>() }
}

fun foo(): Int = 0
