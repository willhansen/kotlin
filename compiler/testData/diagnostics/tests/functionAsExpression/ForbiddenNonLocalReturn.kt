// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE

fun test() {
    fun bar() {
        konst bas = fun() {
            <!RETURN_NOT_ALLOWED!>return@bar<!>
        }
    }

    konst bar = fun() {
        <!RETURN_NOT_ALLOWED!>return@test<!>
    }
}

fun foo() {
    konst bal = bag@ fun () {
        konst bar = fun() {
            <!RETURN_NOT_ALLOWED!>return@bag<!>
        }
        return@bag
    }
}