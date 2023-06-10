// ISSUE: KT-57893
// MODULE: main
internal class A {
    konst x: String? = null
}

class B {
    internal konst x: String? = null
}

class C {
    konst x: String? = null
}

// MODULE: test()(main)
internal fun test(a: A, b: B, c: C) {
    if (a.x != null) {
        <!DEBUG_INFO_SMARTCAST!>a.x<!>.length
    }
    if (b.x != null) {
        <!DEBUG_INFO_SMARTCAST!>b.x<!>.length
    }
    if (c.x != null) {
        <!SMARTCAST_IMPOSSIBLE!>c.x<!>.length
    }
}
