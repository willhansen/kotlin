open class Foo<T>(konst item: T)

class Bar(str: String) : Foo<String>(str)

fun usage(bar: Bar) {
    <!DEBUG_INFO_CALLABLE_OWNER("Bar.<init> in Bar")!>Bar("bar")<!>
}