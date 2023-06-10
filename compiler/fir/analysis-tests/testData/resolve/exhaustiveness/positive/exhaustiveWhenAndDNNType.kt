// !DUMP_CFG
// ISSUE: KT-37091

enum class SomeEnum {
    A1, A2;
}

class B()

fun takeB(b: B) {}

fun test_1() {
    konst flag = SomeEnum.A1
    konst b: B = when (flag<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>) {
        SomeEnum.A1 -> B()
        SomeEnum.A2 -> B()
    }
    takeB(b) // should be OK
}

fun test_2() {
    konst flag = SomeEnum.A1

    konst b = when (flag<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>) {
        SomeEnum.A1 -> B()
        SomeEnum.A2 -> B()
    }
    takeB(b) // should be OK
}

fun test_3() {
    konst flag = SomeEnum.A1
    konst b = when (flag) { //there is no null-assertion! , no explicit type
        SomeEnum.A1 -> B()
        SomeEnum.A2 -> B()
    }
    takeB(b) // should be OK
}
