interface Foo {
    fun bar(): Int
}

konst x by lazy {
    konst foo = object : Foo {
        override fun bar(): Int = 42
    }
    foo.bar()
}