// !DIAGNOSTICS:-USELESS_CAST
// !MARK_DYNAMIC_CALLS

fun test(d: dynamic) {
    d.<!DEBUG_INFO_DYNAMIC!>onAnyVal<!>
    d.<!DEBUG_INFO_DYNAMIC!>onAnyVal<!> = 1

    d?.<!DEBUG_INFO_DYNAMIC!>onAnyVal<!>
    d?.<!DEBUG_INFO_DYNAMIC!>onAnyVal<!> = 1

    run {
        d!!.<!DEBUG_INFO_DYNAMIC!>onAnyVal<!>
    }
    run {
        d!!.<!UNRESOLVED_REFERENCE!>onAnyVal<!> = 1
    }

    d.<!DEBUG_INFO_DYNAMIC!>onNullableAnyVal<!> = 1

    d.<!DEBUG_INFO_DYNAMIC!>onStringVal<!> = 1

    d.<!DEBUG_INFO_DYNAMIC!>onDynamicVal<!> = 1

    (d as String).onStringVal
    (d as Any).onAnyVal
    (d as Any?).onNullableAnyVal
    (d as Any).<!UNRESOLVED_REFERENCE!>onDynamicVal<!>
}

fun testReassignmentWithSafeCall(d: dynamic) {
    d?.<!DEBUG_INFO_DYNAMIC!>onDynamicVal<!> = 1
}

fun testReassignmentWithStaticCalls(d: dynamic) {
    (d as String).<!VAL_REASSIGNMENT!>onStringVal<!> = 1
    (d as Any).<!VAL_REASSIGNMENT!>onAnyVal<!> = 1
    (d as Any?).<!VAL_REASSIGNMENT!>onNullableAnyVal<!> = 1
    (d as Any).<!UNRESOLVED_REFERENCE!>onDynamicVal<!> = 1
}

konst Any.onAnyVal: Int get() = 1
konst Any?.onNullableAnyVal: Int get() = 1
konst String.onStringVal: Int get() = 1
konst <!DYNAMIC_RECEIVER_NOT_ALLOWED!>dynamic<!>.onDynamicVal: Int get() = 1

class C {
    fun test(d: dynamic) {
        d.<!DEBUG_INFO_DYNAMIC!>memberVal<!>
        d.<!DEBUG_INFO_DYNAMIC!>memberVal<!> = 1

        d.<!DEBUG_INFO_DYNAMIC!>memberExtensionVal<!>
        d.<!DEBUG_INFO_DYNAMIC!>memberExtensionVal<!> = 1
    }

    konst memberVal = 1
    konst Any.memberExtensionVal: Int
        get() = 1
}
