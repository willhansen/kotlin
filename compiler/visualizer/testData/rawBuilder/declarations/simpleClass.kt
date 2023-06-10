interface SomeInterface {
    fun foo(x: Int, y: String): String

//      Boolean
//      │
    konst bar: Boolean
}

class SomeClass : SomeInterface {
//              Int   Int
//              │     │
    private konst baz = 42

    override fun foo(x: Int, y: String): String {
//             SomeClass.foo.y: String
//             │ fun (String).plus(Any?): String
//             │ │ SomeClass.foo.x: Int
//             │ │ │ fun (String).plus(Any?): String
//             │ │ │ │ konst (SomeClass).baz: Int
//             │ │ │ │ │
        return y + x + baz
    }

//               Boolean
//               │
    override var bar: Boolean
//              Boolean
//              │
        get() = true
//          Boolean
//          │
        set(konstue) {}

//               Double
//               │
    lateinit var fau: Double
}

inline class InlineClass
