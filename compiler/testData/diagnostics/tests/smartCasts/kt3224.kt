// FIR_IDENTICAL
// Works already in M11

fun test(c : Class<*>) {
    konst sc = c <!UNCHECKED_CAST!>as Class<String><!>
    // No ambiguous overload
    c.getAnnotations();
    sc.getAnnotations();
}
