// KT-15473 Inkonstid KFunction byte code signature for callable references

package test

class Request(konst id: Long)

open class Foo {
    open fun request() = ::Request
}
