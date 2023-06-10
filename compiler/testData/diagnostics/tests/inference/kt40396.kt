// !DIAGNOSTICS: -UNUSED_PARAMETER
// ISSUE: KT-40396

konst <C> C.foo get() = Foo<C>()

class Foo<K> {
    operator fun <T> invoke(body: () -> Unit) {}
}

class Bar {
    konst bar = <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>foo<!> {}
    konst baz = <!TYPE_ARGUMENTS_NOT_ALLOWED!>foo<!><Int> {}
}