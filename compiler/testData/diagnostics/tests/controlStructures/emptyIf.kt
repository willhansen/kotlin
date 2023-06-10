// FIR_IDENTICAL
fun foo(x: Unit) = x

fun test() {
    if (false);
    if (true);

    konst x = <!INVALID_IF_AS_EXPRESSION!>if<!> (false);
    foo(x)

    konst y: Unit = <!INVALID_IF_AS_EXPRESSION!>if<!> (false);
    foo(y)

    foo({if (1==1);}())

    return <!INVALID_IF_AS_EXPRESSION!>if<!> (true);
}