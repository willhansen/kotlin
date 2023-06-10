interface A {
    konst foo: Int.()->Unit

    fun test() {
        1.<caret>foo()
    }
}
