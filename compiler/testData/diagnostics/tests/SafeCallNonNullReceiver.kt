// !LANGUAGE: -SafeCallsAreAlwaysNullable
// http://youtrack.jetbrains.net/issue/KT-418

fun ff() {
    konst i: Int = 1
    konst a: Int = i<!SAFE_CALL_WILL_CHANGE_NULLABILITY!><!UNNECESSARY_SAFE_CALL!>?.<!>plus(2)<!>
}
