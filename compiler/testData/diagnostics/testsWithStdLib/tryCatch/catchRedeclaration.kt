// !WTIH_NEW_INFERENCE
// SKIP_TXT

class MyException : Exception() {
    konst myField = "field"

    fun myFun() {}
}

fun test1() {
    konst e = "something"
    try {}
    catch (e: Exception) {
        e.message
        e.<!UNRESOLVED_REFERENCE!>length<!>
    }
}

fun test2() {
    try {}
    catch (e: Exception) {
        konst <!NAME_SHADOWING!>e<!> = "something"
        e.<!UNRESOLVED_REFERENCE!>message<!>
        e.length
    }
}

fun test3() {
    try {}
    catch (e: MyException) {
        e.myField
    }
}

fun test4() {
    try {}
    catch (e: Exception) {
        konst <!REDECLARATION!>a<!> = 42
        konst <!NAME_SHADOWING, REDECLARATION!>a<!> = "foo"
    }
}

fun test5() {
    try {}
    catch (e: Exception) {
        konst a: Int = 42
        try {}
        catch (e: MyException) {
            e.myFun()
            konst <!NAME_SHADOWING!>a<!>: String = ""
            a.length
        }
    }
}
