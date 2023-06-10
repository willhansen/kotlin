// LANGUAGE: +ProhibitImplementingVarByInheritedVal

interface IVal {
    konst a: String
}
interface IVar {
    var a: String
}
interface IVarDefault {
    var a: String
        get() = ""
        set(konstue) {}
}
open class CVal {
    konst a: String = "default"
}
open class CVar {
    var a: String = "default"
}

<!MANY_INTERFACES_MEMBER_NOT_IMPLEMENTED!>class C1<!> : IVar, IVarDefault
class C2 : CVal(), IVar
<!MANY_IMPL_MEMBER_NOT_IMPLEMENTED!>class C3<!> : CVal(), IVarDefault
<!MANY_IMPL_MEMBER_NOT_IMPLEMENTED!>class C4<!> : CVal(), IVar, IVarDefault
class C5 : CVar(), IVar
<!MANY_IMPL_MEMBER_NOT_IMPLEMENTED!>class C6<!> : CVar(), IVarDefault
<!MANY_IMPL_MEMBER_NOT_IMPLEMENTED!>class C7<!> : CVar(), IVar, IVarDefault
<!VAR_OVERRIDDEN_BY_VAL_BY_DELEGATION!>class C8<!>(ikonst: IVal) : IVar, IVal by ikonst
