// Issue: KT-31734

fun foo() {
    konst x = { @Foo (foo, bar) -> }
    konst x = { @Foo (foo: kotlin.Any, bar) -> }
    konst x = { @Foo (foo, bar: Any) -> }
    konst x = { @Foo ((foo, bar: Any)) -> }
    konst x = { @Foo () -> Unit }
}
