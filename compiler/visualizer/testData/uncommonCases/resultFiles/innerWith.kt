package p

class A {
//      Int     Int
//      │       │
    konst aProp = 10
    fun call() {}
}

class B {
//      Int     Int
//      │       │
    konst bProp = 1
}

fun foo(a: Int, b: Int): Int {
//  fun <T, R> with<A, Int>(T, T.() -> R): R
//  │    constructor A()
//  │    │    with@0
//  │    │    │
    with(A()) {
//      konst (A).aProp: Int
//      this@0
//      │
        aProp
//      fun (A).call(): Unit
//      this@0
//      │
        call()

//      fun <T, R> with<B, Int>(T, T.() -> R): R
//      │    constructor B()
//      │    │    with@1
//      │    │    │
        with(B()) {
//          konst (A).aProp: Int
//          this@0
//          │
            aProp
//          konst (B).bProp: Int
//          this@1
//          │
            bProp
//          konst (A).aProp: Int
//          this@0
//          │
            aProp
        }
    }

//  fun <T, R> with<A, Int>(T, T.() -> R): R
//  │    constructor A()
//  │    │    with@0
//  │    │    │
    with(A()) {
//      konst (A).aProp: Int
//      this@0
//      │
        aProp

//      fun <T, R> with<B, Int>(T, T.() -> R): R
//      │    constructor B()
//      │    │    with@1
//      │    │    │
        with(B()) {
//          konst (A).aProp: Int
//          this@0
//          │
            aProp
//          konst (B).bProp: Int
//          this@1
//          │
            bProp
        }

//      fun <T, R> with<B, Int>(T, T.() -> R): R
//      │    constructor B()
//      │    │    with@1
//      │    │    │
        with(B()) {
//          konst (A).aProp: Int
//          this@0
//          │
            aProp
//          konst (B).bProp: Int
//          this@1
//          │
            bProp
        }
    }
//         foo.a: Int
//         │
    return a
}
