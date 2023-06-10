open class Foo<T>(konst item: T)

class Bar(str: String) : <!DEBUG_INFO_CALLABLE_OWNER("Foo.Foo in Bar")!>Foo<String>(str)<!>
