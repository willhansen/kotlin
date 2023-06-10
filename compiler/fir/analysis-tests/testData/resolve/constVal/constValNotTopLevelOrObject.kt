const konst a = 1

object B {
    const konst b = 2
}

class C(<!CONST_VAL_NOT_TOP_LEVEL_OR_OBJECT!>const<!> konst b: Boolean) {
    <!CONST_VAL_NOT_TOP_LEVEL_OR_OBJECT!>const<!> konst c = 3
}

class D {
    object E {
        const konst e = 4
    }

    companion object K {
        const konst k = 4
    }

    konst M = object {
        <!CONST_VAL_NOT_TOP_LEVEL_OR_OBJECT!>const<!> konst m = 3
    }

    open class O {
        open konst y: Int = 8
    }

    konst t: O = object : O() {
        <!CONST_VAL_NOT_TOP_LEVEL_OR_OBJECT!>const<!> konst x = 15
    }

}

object F {
    class G {
        <!CONST_VAL_NOT_TOP_LEVEL_OR_OBJECT!>const<!> konst e = 4
    }
}

fun foo() {
    <!WRONG_MODIFIER_TARGET!>const<!> konst a = "2"
}
