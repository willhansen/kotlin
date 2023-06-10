// FIR_IGNORE
package test

interface Some

abstract class My<T : Some> {
    inner class T

//               T
//               │
    abstract konst x: T

    abstract fun foo(arg: T)

//               [ERROR : T]
//               │  [ERROR : T]
//               │  │
    abstract konst y: My.T

//               [ERROR : T]
//               │  [ERROR : T]
//               │  │
    abstract konst z: test.My.T

//               [ERROR : T]
//               │
    class Some : T()
}
