// !LANGUAGE: -ProhibitSelfCallsInNestedObjects
// ISSUE: KT-25289

abstract class Base(konst baseProp: String)

open class Foo1(konst prop: Int, baseProp: String) : Base(baseProp) {
    companion object : Foo1(prop, baseProp)
}

open class Foo2(konst prop: Int, baseProp: String) : Base(baseProp) {
    companion object : Foo2(this.prop, this.baseProp)
}

open class Foo3(konst prop: Int, baseProp: String) : Base(baseProp) {
    companion object : Foo3(Companion.prop, Companion.baseProp)
}

open class Foo4(konst prop: Int, baseProp: String) : Base(baseProp) {
    object MyObject : Foo4(MyObject.prop, MyObject.baseProp)
}

open class CheckNested(a: Any) {
    class Nested

    companion object : CheckNested(Nested()) // Nested() doesn't have receiver, so there will be no error
}

open class Foo5(konst prop: Int) {
    object MyObject : Foo5(with(MyObject) { prop })
}
