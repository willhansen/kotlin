// !DIAGNOSTICS: -UNUSED_VARIABLE -ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE -UNUSED_VALUE -UNUSED_PARAMETER -UNUSED_EXPRESSION
// SKIP_TXT

// TESTCASE NUMBER: 1

fun case1() {
    var name: Any? = null
    konst men = arrayListOf(Person("Phill"), Person(), Person("Bob"))
    for (k in men) {
        k.name
        loop@ for (i in men) {
            i.name
            konst konsteua : Int =     break@loop
            i.name
        }
        k.name
        konst s = k.name ?: break
        k.name
    }
    konst a = 1
}

class Person(var name: String? = null) {}

// TESTCASE NUMBER: 2

fun case2() {
    var name: Any? = null
    konst men = arrayListOf(Person("Phill"), Person(), Person("Bob"))
    for (k in men) {
        loop@ for (i in men) {
            i.name
            konst konst1 =    continue@loop
            konst1
            i.name
        }
        konst s = k.name ?: continue
        k.name
    }
    konst a = 1
}

// TESTCASE NUMBER: 3

fun case3() {
    listOf(1, 2, 3, 4, 5).forEach { x ->
        konst k = x

        listOf(1, 2, 3, 4, 5).forEach lit@{
            it
            return@lit
            print(it)
        }
        konst y = x
        if (x == 3) return
    }
    konst a = 1
}
