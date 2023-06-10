// FIR_IDENTICAL
open class C(konst x: Int)

class D : C {
    constructor() : super(
            {
                konst s = ""
                <!INSTANCE_ACCESS_BEFORE_SUPER_CALL!>s<!>()
                <!INSTANCE_ACCESS_BEFORE_SUPER_CALL!>""()<!>
                42
            }())

    operator fun String.invoke() { }
}
