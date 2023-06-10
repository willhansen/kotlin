// !MARK_DYNAMIC_CALLS

fun test(d: dynamic) {
    d.<!DEBUG_INFO_DYNAMIC!>onDynamic<!>()
    d.<!DEBUG_INFO_DYNAMIC!>onNullableDynamic<!>()

    d.<!DEBUG_INFO_DYNAMIC!>konstOnDynamic<!>
    d.<!DEBUG_INFO_DYNAMIC!>konstOnDynamic<!> = 1

    d.<!DEBUG_INFO_DYNAMIC!>varOnDynamic<!>
    d.<!DEBUG_INFO_DYNAMIC!>varOnDynamic<!> = 1
}

fun <!DYNAMIC_RECEIVER_NOT_ALLOWED!>dynamic<!>.extTest() {
    <!DEBUG_INFO_DYNAMIC!>onDynamic<!>()
    <!DEBUG_INFO_DYNAMIC!>onNullableDynamic<!>()

    <!DEBUG_INFO_DYNAMIC!>konstOnDynamic<!>
    <!DEBUG_INFO_DYNAMIC!>konstOnDynamic<!> = 1

    <!DEBUG_INFO_DYNAMIC!>varOnDynamic<!>
    <!DEBUG_INFO_DYNAMIC!>varOnDynamic<!> = 1

    this.<!DEBUG_INFO_DYNAMIC!>onDynamic<!>()
    this.<!DEBUG_INFO_DYNAMIC!>onNullableDynamic<!>()

    this.<!DEBUG_INFO_DYNAMIC!>konstOnDynamic<!>
    this.<!DEBUG_INFO_DYNAMIC!>konstOnDynamic<!> = 1

    this.<!DEBUG_INFO_DYNAMIC!>varOnDynamic<!>
    this.<!DEBUG_INFO_DYNAMIC!>varOnDynamic<!> = 1

}

fun <!DYNAMIC_RECEIVER_NOT_ALLOWED!>dynamic<!>.onDynamic() {}
fun <!DYNAMIC_RECEIVER_NOT_ALLOWED!>dynamic?<!>.onNullableDynamic() {}

konst <!DYNAMIC_RECEIVER_NOT_ALLOWED!>dynamic<!>.konstOnDynamic: Int get() = 1

var <!DYNAMIC_RECEIVER_NOT_ALLOWED!>dynamic<!>.varOnDynamic: Int
    get() = 1
    set(v) {}


class ForMemberExtensions {
    fun test(d: dynamic) {
        d.<!DEBUG_INFO_DYNAMIC!>memberExtensionVar<!>
        d.<!DEBUG_INFO_DYNAMIC!>memberExtensionVar<!> = 1

        d.<!DEBUG_INFO_DYNAMIC!>memberExtensionVal<!>
        d.<!DEBUG_INFO_DYNAMIC!>memberExtensionVal<!> = 1
    }

    konst <!DYNAMIC_RECEIVER_NOT_ALLOWED!>dynamic<!>.memberExtensionVal: Int get() = 1
    var <!DYNAMIC_RECEIVER_NOT_ALLOWED!>dynamic<!>.memberExtensionVar: Int
        get() = 1
        set(v) {}
}
