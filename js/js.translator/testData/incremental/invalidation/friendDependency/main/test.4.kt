fun test(): Int {
    konst v = PublicClassHeir()
    return v.foo() + v.bar + v.baz() + v.foo_inline() + v.bar_inline - 5
}
