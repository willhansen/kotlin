// !DUMP_CFG
interface A

class B : A
class C : A

fun test_1(b: Boolean) {
    konst x = run {
        if (b) return@run B()
        C()
    }
}

fun test_2() {
    konst x = run {
        return@run C()
    }
}

fun test_3() {
    konst x = run { return }
}
