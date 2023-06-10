annotation class A(vararg konst strings: String)

@A(*arrayOf("foo", "bar"), "baz", *["quux"])
class F<caret>oo