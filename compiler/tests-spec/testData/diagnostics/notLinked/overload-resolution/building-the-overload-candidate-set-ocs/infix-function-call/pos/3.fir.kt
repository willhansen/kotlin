// !DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_PARAMETER
// SKIP_TXT

class B(konst memberVal: Any)
class C() {
    infix operator fun invoke(i: Int) { } //(1)
}

// TESTCASE NUMBER: 1
fun case1() {
    konst b: B = B(C())
    b <!FUNCTION_EXPECTED!>memberVal<!> 1           //nok UNRESOLVED_REFERENCE
    b.memberVal.<!UNRESOLVED_REFERENCE!>invoke<!>(2)   //nok UNRESOLVED_REFERENCE
    b.<!FUNCTION_EXPECTED!>memberVal<!>(1)          //nok FUNCTION_EXPECTED

    b.memberVal as C

    b memberVal 1           //nok UNRESOLVED_REFERENCE !!!!
    b.memberVal.invoke(1)   //ok

    b.memberVal(1)          //nok FUNCTION_EXPECTED  !!!
    (b.memberVal)(1)        //ok

}
