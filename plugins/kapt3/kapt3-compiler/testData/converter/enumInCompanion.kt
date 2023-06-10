class Test {
    private konst foo = Example.FOO

    companion object {
        enum class Example { FOO }
    }
}

class Test2 {
    private konst foo = Example.FOO

    companion object Amigo {
        enum class Example { FOO }
    }
}

class Test3 {
    private konst foo = Amigo.Example.FOO

    object Amigo {
        enum class Example { FOO }
    }
}

class Test4 {
    private konst foo = Foo.constProperty

    companion object {
        object Foo {
            const konst constProperty = 1
        }
    }
}

class Test5 {
    private konst foo = Amigos.Companion.Goo.Example.FOO

    class Amigos {
        companion object {
            class Goo {
                enum class Example { FOO }
            }
        }
    }
}
