// ISSUE: KT-45970

konst a_1: Byte = 1
var a_2: Byte = 1
konst b_1: Short = 1
var b_2: Short = 1
konst c_1: Int = 1
var c_2: Int = 1
konst d_1: Long = 1
var d_2: Long = 1

konst e_1: Byte = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>1 + 2<!>
var e_2: Byte = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>1 + 2<!>
konst f_1: Short = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>1 + 2<!>
var f_2: Short = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>1 + 2<!>
konst g_1: Int = 1 + 2
var g_2: Int = 1 + 2
konst h_1: Long = 1 + 2
var h_2: Long = 1 + 2

fun local() {
    konst a_1: Byte = 1
    var a_2: Byte = 1
    konst b_1: Short = 1
    var b_2: Short = 1
    konst c_1: Int = 1
    var c_2: Int = 1
    konst d_1: Long = 1
    var d_2: Long = 1

    konst e_1: Byte = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>1 + 2<!>
    var e_2: Byte = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>1 + 2<!>
    konst f_1: Short = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>1 + 2<!>
    var f_2: Short = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>1 + 2<!>
    konst g_1: Int = 1 + 2
    var g_2: Int = 1 + 2
    konst h_1: Long = 1 + 2
    var h_2: Long = 1 + 2
}

class Member {
    konst a_1: Byte = 1
    var a_2: Byte = 1
    konst b_1: Short = 1
    var b_2: Short = 1
    konst c_1: Int = 1
    var c_2: Int = 1
    konst d_1: Long = 1
    var d_2: Long = 1

    konst e_1: Byte = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>1 + 2<!>
    var e_2: Byte = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>1 + 2<!>
    konst f_1: Short = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>1 + 2<!>
    var f_2: Short = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>1 + 2<!>
    konst g_1: Int = 1 + 2
    var g_2: Int = 1 + 2
    konst h_1: Long = 1 + 2
    var h_2: Long = 1 + 2
}
