// ISSUE: KT-37070

class A

fun test(a: A) {

    konst lambda = a.let {
        { it }
    }

    konst alsoA = lambda()
    takeA(alsoA)
}

fun takeA(a: A) {}
