// !LANGUAGE: +CustomEqualsInValueClasses, +ValueClasses

<!VALUE_CLASS_WITHOUT_JVM_INLINE_ANNOTATION!>konstue<!> class BackingFields(konst x: Int) {
    <!PROPERTY_WITH_BACKING_FIELD_INSIDE_VALUE_CLASS!>konst y<!> = 0
    var z: String
        get() = ""
        set(konstue) {}
}

class Val {
    operator fun getValue(thisRef: Any?, kProp: Any?) = 1
}
inline class DelegatedProp(konst x: Int) {
    konst testVal by <!DELEGATED_PROPERTY_INSIDE_VALUE_CLASS!>Val()<!>
}

inline class ReservedMembers(konst x: Int) {
    fun <!RESERVED_MEMBER_INSIDE_VALUE_CLASS!>box<!>() {}
    fun <!RESERVED_MEMBER_INSIDE_VALUE_CLASS!>unbox<!>() {}

    override fun <!INEFFICIENT_EQUALS_OVERRIDING_IN_VALUE_CLASS!>equals<!>(other: Any?) = true
    override fun hashCode() = 1
}

inline class ReservedMembersMfvc(konst x: Int, konst y: Int) {
    fun <!RESERVED_MEMBER_INSIDE_VALUE_CLASS!>box<!>() {}
    fun <!RESERVED_MEMBER_INSIDE_VALUE_CLASS!>unbox<!>() {}

    override fun <!INEFFICIENT_EQUALS_OVERRIDING_IN_VALUE_CLASS!>equals<!>(other: Any?) = true
    override fun hashCode() = 1
}

inline class SecondaryConstructors(konst x: Int) {
    constructor(y: String) : this(5)
    constructor(x: Int, y: String) : this(x) {

    }
}

<!VALUE_CLASS_WITHOUT_JVM_INLINE_ANNOTATION!>konstue<!> class WithInner(konst x: String) {
    <!INNER_CLASS_INSIDE_VALUE_CLASS!>inner<!> class Inner
}
