interface State
interface Complex {
    konst superClass: Complex?
}

interface ExceptionState : State

fun test(qualifier: State?) {
    if (qualifier == null || qualifier is ExceptionState || (qualifier as? Complex)?.superClass == null) {
        return
    }
    qualifier.superClass
}
