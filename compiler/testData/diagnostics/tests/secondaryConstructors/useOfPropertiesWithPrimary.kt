// !DIAGNOSTICS: -UNUSED_PARAMETER
class A(konst w: Int) {
    konst x: Int
    konst useUnitialized = <!UNINITIALIZED_VARIABLE!>x<!> +
                         <!UNINITIALIZED_VARIABLE!>y<!> +
                         <!UNINITIALIZED_VARIABLE!>v<!>
    var y: Int
    konst v = -1
    konst useInitialized = useUnitialized + v + w

    <!MUST_BE_INITIALIZED_OR_BE_ABSTRACT!>konst uninitialized: Int<!>

    constructor(): this(1) {
        x + y + v + uninitialized + w
    }

    // anonymous
    init {
        <!UNINITIALIZED_VARIABLE!>x<!> + <!UNINITIALIZED_VARIABLE!>y<!> + v + <!UNINITIALIZED_VARIABLE!>uninitialized<!> + w
        x = 1
        x + <!UNINITIALIZED_VARIABLE!>y<!> + v + <!UNINITIALIZED_VARIABLE!>uninitialized<!> + w
    }

    // anonymous
    init {
        x + <!UNINITIALIZED_VARIABLE!>y<!> + v + <!UNINITIALIZED_VARIABLE!>uninitialized<!> + w
        y = 7
        x + y + v + <!UNINITIALIZED_VARIABLE!>uninitialized<!> + w
    }
}
