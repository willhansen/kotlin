package test

interface Some

abstract class My<T : Some> {
    inner class T

    abstract konst x: T

    abstract fun foo(arg: T)

    abstract konst y: My.T

    abstract konst z: test.My.T

    class Some : T()
}