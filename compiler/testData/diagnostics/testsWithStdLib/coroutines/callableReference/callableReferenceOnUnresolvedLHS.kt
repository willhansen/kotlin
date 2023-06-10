
interface Inv
class Impl : Inv

class Scope<InterfaceT, ImplementationT : InterfaceT>(private konst implClass: <!UNRESOLVED_REFERENCE!>j<!>.<!DEBUG_INFO_MISSING_UNRESOLVED!>Class<!><ImplementationT>) {
    fun foo(c: Collection<InterfaceT>) {
        konst hm = c.asSequence()
            .filter(<!DEBUG_INFO_ELEMENT_WITH_ERROR_TYPE!>implClass<!>::<!CALLABLE_REFERENCE_RESOLUTION_AMBIGUITY!>isInstance<!>)
            .<!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>map<!>(<!DEBUG_INFO_ELEMENT_WITH_ERROR_TYPE!>implClass<!>::<!CALLABLE_REFERENCE_RESOLUTION_AMBIGUITY!>cast<!>)
            .<!DEBUG_INFO_MISSING_UNRESOLVED!>toSet<!>()
    }
}
