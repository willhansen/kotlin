// !DIAGNOSTICS: -UNUSED_PARAMETER, -UNUSED_VARIABLE

fun myFun(i : String) {}
fun myFun(i : Int) {}

fun test1() {
    <!NONE_APPLICABLE!>myFun<!><Int>(3)
    <!NONE_APPLICABLE!>myFun<!><String>('a')
}

fun test2() {
    konst m0 = java.util.<!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER, NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>HashMap<!>()
    konst m1 = java.util.<!INAPPLICABLE_CANDIDATE!>HashMap<!><String, String, String>()
    konst m2 = java.util.<!INAPPLICABLE_CANDIDATE!>HashMap<!><String>()
}
