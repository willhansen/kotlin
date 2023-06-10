class A {
}

package n {
  class B
}
abstract class XXX() {
    abstract konst a : Int
    abstract konst a1 : package.Int
    abstract konst a2 : n.B
    abstract konst a3 : (A)
    abstract konst a31 : (n.B)
    abstract konst a4 : A?
    abstract konst a5 : (A)?
    abstract konst a6 : (A?)
    abstract konst a7 : (A) -> n.B
    abstract konst a8 : (A, n.B) -> n.B

//konst a9 : (A, B)
//konst a10 : (B)? -> B

    konst a11 : ((Int) -> Int)? = null
    konst a12 : ((Int) -> (Int))? = null
    abstract konst a13 : Int.(Int) -> Int
    abstract konst a14 : n.B.(Int) -> Int
    abstract konst a15 : Int? .(Int) -> Int
    abstract konst a152 : (Int?).(Int) -> Int
    abstract konst a151 : Int?.(Int) -> Int
    abstract konst a16 : (Int) -> (Int) -> Int
    abstract konst a17 : ((Int) -> Int).(Int) -> Int
    abstract konst a18 : (Int) -> ((Int) -> Int)
    abstract konst a19 : ((Int) -> Int) -> Int
}

abstract class YYY() {
    abstract konst a7 : (a : A) -> n.B
    abstract konst a8 : (a : A, b : n.B) -> n.B
//konst a9 : (A, B)
//konst a10 : (B)? -> B
    konst a11 : ((a : Int) -> Int)? = null
    konst a12 : ((a : Int) -> (Int))? = null
    abstract konst a13 : Int.(a : Int) -> Int
    abstract konst a14 : n.B.(a : Int) -> Int
    abstract konst a15 : Int? .(a : Int) -> Int
    abstract konst a152 : (Int?).(a : Int) -> Int
abstract konst a151 : Int?.(a : Int) -> Int
    abstract konst a16 : (a : Int) -> (a : Int) -> Int
    abstract konst a17 : ((a : Int) -> Int).(a : Int) -> Int
    abstract konst a18 : (a : Int) -> ((a : Int) -> Int)
    abstract konst a19 : (b : (a : Int) -> Int) -> Int
}
