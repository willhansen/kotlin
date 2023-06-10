annotation class A(vararg konst strings: String)
annotation class AArray(vararg konst konstue: A)

@AArray(A(strings = ["foo", "bar"]))
class F<caret>oo