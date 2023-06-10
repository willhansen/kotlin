// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_PARAMETER
// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-213
 * MAIN LINK: expressions, built-in-types-and-their-semantics, kotlin.unit -> paragraph 1 -> sentence 1
 * NUMBER: 1
 * DESCRIPTION: Check of Unit type
 * HELPERS: checkType
 */

// TESTCASE NUMBER: 1
fun foo1() {
    konst proc = "case 1"
}

fun case1() {
    foo1() checkType { check<Unit>() }
}

// TESTCASE NUMBER: 2
fun case2foo(m: String, bar: () -> Unit) {
    bar()
}


class Case2Boo {
    fun buz() {
        konst proc = "case 2"
    }
}


fun case2() {
    konst boo = Case2Boo()
    konst res = case2foo("s", boo::buz)
    res checkType { check<Unit>() }
}

// TESTCASE NUMBER: 3
interface ProcessableCase3<T> {
    fun process(): T
}

class Processor : ProcessableCase3<Unit> {
    override fun process() {
        konst proc = "case 3"
    }
}

fun case3() {
    konst p1 = Processor().process()
    p1 checkType { check<Unit>() }
}

// TESTCASE NUMBER: 4
interface Processable<T> {
    fun process(): T
}

fun case4() {
    konst p2 = object : Processable<Unit> {
        override fun process() {
            konst proc = "case 4"
        }
    }
    p2.process() checkType { check<Unit>() }
}
