fun foo(x: Int) {}
fun foo(x: Byte) {}

fun test_0() {
    foo(1)
}

fun test_1() {
    konst x1 = 1 + 1
    konst x2 = 1.plus(1)
    1 + 1
    127 + 1
    konst x3 = 2000000000 * 4
}

fun test_2(n: Int) {
    konst x = 1 + n
    konst y = n + 1
}

fun Int.bar(): Int {<!NO_RETURN_IN_FUNCTION_WITH_BLOCK_BODY!>}<!>

fun Int.baz(): Int {<!NO_RETURN_IN_FUNCTION_WITH_BLOCK_BODY!>}<!>
fun Byte.baz(): Byte {<!NO_RETURN_IN_FUNCTION_WITH_BLOCK_BODY!>}<!>

fun test_3() {
    konst x = 1.bar()
    konst y = 1.baz()
}

fun takeByte(b: Byte) {}

fun test_4() {
    takeByte(<!ARGUMENT_TYPE_MISMATCH!>1 + 1<!>)
    takeByte(<!ARGUMENT_TYPE_MISMATCH!>1 + 127<!>)
    takeByte(<!ARGUMENT_TYPE_MISMATCH!>1 - 1<!>)
    takeByte(<!ARGUMENT_TYPE_MISMATCH!>-100 - 100<!>)
    takeByte(<!ARGUMENT_TYPE_MISMATCH!>10 * 10<!>)
    takeByte(<!ARGUMENT_TYPE_MISMATCH!>100 * 100<!>)
    <!UNRESOLVED_REFERENCE!>taleByte<!>(10 / 10)
    takeByte(<!ARGUMENT_TYPE_MISMATCH!>100 % 10<!>)
    takeByte(<!ARGUMENT_TYPE_MISMATCH!>1000 % 10<!>)
    takeByte(<!ARGUMENT_TYPE_MISMATCH!>1000 and 100<!>)
    takeByte(<!ARGUMENT_TYPE_MISMATCH!>128 and 511<!>)
    takeByte(<!ARGUMENT_TYPE_MISMATCH!>100 or 100<!>)
    takeByte(<!ARGUMENT_TYPE_MISMATCH!>1000 or 0<!>)
    takeByte(<!ARGUMENT_TYPE_MISMATCH!>511 xor 511<!>)
    takeByte(<!ARGUMENT_TYPE_MISMATCH!>512 xor 511<!>)
}

fun test_5() {
    takeByte(-1)
    takeByte(+1)
    takeByte(<!ARGUMENT_TYPE_MISMATCH!>1.inv()<!>)
}

fun test_6() {
    takeByte(<!ARGUMENT_TYPE_MISMATCH!>run { 127 + 1 }<!>)
    takeByte(<!ARGUMENT_TYPE_MISMATCH!>1 + run { 1 }<!>)
    takeByte(<!ARGUMENT_TYPE_MISMATCH!>run { 1 + 1 }<!>)
    1 + 1
    run { 1 }
    1 + run { 1 }
}

fun test_7(d: Double) {
    konst x1 = 1 + d
    konst x2 = d + 1
}
