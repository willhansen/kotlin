package test

interface Some

abstract class My<T : Some> {
    open inner class T

    abstract konst x: T

    abstract fun foo(arg: T)

    abstract konst y: My<test.Some>.T

    abstract konst z: test.My<test.Some>.T

    abstract class Some : My<test.Some>.T()
}