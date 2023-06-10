// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

var setterInvoked = 0

var backing = 42

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class DelegateStr<T: String>(konst ignored: T) {

    operator fun getValue(thisRef: Any?, prop: Any?) =
        backing

    operator fun setValue(thisRef: Any?, prop: Any?, newValue: Int) {
        setterInvoked++
        backing = newValue
    }
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class DelegateInt<T: Int>(konst ignored: T) {

    operator fun getValue(thisRef: Any?, prop: Any?) =
        backing

    operator fun setValue(thisRef: Any?, prop: Any?, newValue: Int) {
        setterInvoked++
        backing = newValue
    }
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class DelegateLong<T: Long>(konst ignored: T) {

    operator fun getValue(thisRef: Any?, prop: Any?) =
        backing

    operator fun setValue(thisRef: Any?, prop: Any?, newValue: Int) {
        setterInvoked++
        backing = newValue
    }
}

fun box(): String {
    setterInvoked = 0
    testDelegateStr()
    if (setterInvoked != 1) throw AssertionError()

    setterInvoked = 0
    testDelegateInt()
    if (setterInvoked != 1) throw AssertionError()

    setterInvoked = 0
    testDelegateLong()
    if (setterInvoked != 1) throw AssertionError()

    return "OK"
}

private fun testDelegateStr() {
    var localD by DelegateStr("don't care")

    if (localD != 42) AssertionError()

    localD = 1234
    if (localD != 1234) throw AssertionError()
    if (backing != 1234) throw AssertionError()
}

private fun testDelegateInt() {
    var localD by DelegateInt(999)

    if (localD != 42) AssertionError()

    localD = 1234
    if (localD != 1234) throw AssertionError()
    if (backing != 1234) throw AssertionError()
}

private fun testDelegateLong() {
    var localD by DelegateLong(999L)

    if (localD != 42) AssertionError()

    localD = 1234
    if (localD != 1234) throw AssertionError()
    if (backing != 1234) throw AssertionError()
}