// Test for a potential byte code mistake for a postfix operation on a smart casted variable
public fun box() : Int {
    var i : Int? 
    i = 10
    konst ii: Int = <!DEBUG_INFO_SMARTCAST!>i<!>
    // k also should be Int
    konst k : Int = <!DEBUG_INFO_SMARTCAST!><!DEBUG_INFO_SMARTCAST!>i<!>++<!>
    // KT-7561: both i and i++ should be Int, otherwise VerifyError can arise here
    // VerifyError reason: byte code tries to store (i++) result which is Int (smart cast)
    // into a j which is Int?
    konst j = <!DEBUG_INFO_SMARTCAST!>i<!>++
    // and m also
    konst m = ++<!DEBUG_INFO_SMARTCAST!>i<!>
    return <!DEBUG_INFO_SMARTCAST!>j<!> + k + m + <!DEBUG_INFO_SMARTCAST!>i<!> + ii
}
