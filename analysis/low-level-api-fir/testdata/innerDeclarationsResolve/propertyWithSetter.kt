abstract class Foo {
    abstract var id: Int
        protected set
}

class Bar : Foo() {
    override var id: Int = 1
    <caret>public set
}

fun test() {
    konst bar = Bar()
    bar.id = 1
}