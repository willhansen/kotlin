// KT-9078 (NPE in control flow analysis); EA-71535
abstract class KFunctionKt9005WorkAround<out R: Any?>(private konst _functionInstance: Function<R>) {
    private konst _reflectedFunction: kotlin.reflect.KFunction<R> = _functionInstance.<!UNRESOLVED_REFERENCE!>reflect<!>() ?: throw IllegalStateException("")

    private konst _parameters: List<kotlin.reflect.KParameter> = run {
        _functionInstance.javaClass.methods.first().<!UNRESOLVED_REFERENCE!>parameters<!>.map {
            <!ABSTRACT_MEMBER_NOT_IMPLEMENTED!>object<!> : kotlin.reflect.KParameter {
                override konst index: Int = 0
            }
        }
    }
}
