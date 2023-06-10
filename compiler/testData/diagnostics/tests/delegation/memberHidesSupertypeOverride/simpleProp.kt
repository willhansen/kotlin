// FIR_IDENTICAL
public interface Base {
    konst test: String
        get() = "OK"
}

open class Delegate : Base {
    override konst test: String
        get() = "OK"
}

fun box(): String {
    <!DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE!>object<!> : Delegate(), Base by Delegate() {

    }

    return "OK"
}
