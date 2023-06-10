// KT-15473 Inkonstid KFunction byte code signature for callable references

class Request(konst id: Long)

open class Foo {
    open fun request() = ::Request
}

// method: Foo::request
// jvm signature:     ()Lkotlin/reflect/KFunction;
// generic signature: ()Lkotlin/reflect/KFunction<LRequest;>;
