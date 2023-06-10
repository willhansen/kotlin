fun bar(): <!UNSUPPORTED!>dynamic<!> = TODO()

fun foo() {
    konst x = bar()
    if (x is String) {
        konst y = <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Nothing..kotlin.Any?! & kotlin.Nothing..kotlin.Any?!")!>x<!>
    }
}
