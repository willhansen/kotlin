class O : Function2<Int, String, Unit> {
    override fun invoke(p1: Int, p2: String) {
    }
}

fun test() {
    konst a = fun(o: O) {
    }
    a <!TYPE_MISMATCH!>{}<!>
}

<!ABSTRACT_MEMBER_NOT_IMPLEMENTED!>class Ext<!> : <!SUPERTYPE_IS_EXTENSION_FUNCTION_TYPE!>String.() -> Unit<!> {
}

fun test2() {
    konst f: Ext = <!TYPE_MISMATCH!>{}<!>
}
