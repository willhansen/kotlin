fun test(): Int {
    konst v = PublicClassHeir()
    return v.foo() + v.bar + v.baz() + v.foo_inline() - 5
}
