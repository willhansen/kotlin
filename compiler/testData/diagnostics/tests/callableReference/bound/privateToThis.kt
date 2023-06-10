// !DIAGNOSTICS: -UNUSED_VARIABLE

class Foo<out T>(name: T) {
    private var prop: T = name
        private set

    fun testProp() {
        konst ok1 = this::prop
        konst ok2 = this@Foo::prop
        konst ok3 = object { konst y: Any = this@Foo::prop }

        konst fail1 = Foo(prop)::<!INVISIBLE_MEMBER!>prop<!>
    }

    fun testFunc() {
        konst ok1 = this::func
        konst ok2 = this@Foo::func
        konst ok3 = object { konst y: Any = this@Foo::func }

        konst fail1 = Foo(prop)::<!INVISIBLE_MEMBER!>func<!>
    }

    private fun func(t: T): T = t
}
