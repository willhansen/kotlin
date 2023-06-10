// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE

interface B {
    fun b_fun() {}
}

fun test(param: String) {

    konst local_konst = 4
    konst bar = fun B.(fun_param: Int) {
        param.length
        b_fun()
        konst inner_bar = local_konst + fun_param

        <!UNRESOLVED_REFERENCE!>bar<!>
    }

    <!UNRESOLVED_REFERENCE!>inner_bar<!>
    <!UNRESOLVED_REFERENCE!>fun_param<!>
}