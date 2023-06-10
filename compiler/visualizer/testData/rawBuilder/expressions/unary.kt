// FIR_IGNORE
// WITH_STDLIB
fun test() {
//      Int Int
//      │   │
    var x = 0
//           var test.x: Int
//      Int  │fun (Int).inc(): Int
//      │    ││
    konst x1 = x++
//           fun (Int).inc(): Int
//      Int  │ var test.x: Int
//      │    │ │
    konst x2 = ++x
//           fun (Int).dec(): Int
//      Int  │ var test.x: Int
//      │    │ │
    konst x3 = --x
//           var test.x: Int
//      Int  │fun (Int).dec(): Int
//      │    ││
    konst x4 = x--
//  Unit
//  │   fun (Boolean).not(): Boolean
//  │   │ var test.x: Int
//  │   │ │ EQ operator call
//  │   │ │ │  Int
//  │   │ │ │  │
    if (!(x == 0)) {
//      fun io/println(Any?): Unit
//      │
        println("000")
    }
}

class X(konst i: Int)

fun test2(x: X) {
//           test2.x: X
//           │ konst (X).i: Int
//      Int  │ │fun (Int).inc(): Int
//      │    │ ││
    konst x1 = x.i++
//           fun (Int).inc(): Int
//           │ test2.x: X
//      Int  │ │ konst (X).i: Int
//      │    │ │ │
    konst x2 = ++x.i
}

fun test3(arr: Array<Int>) {
//           test3.arr: Array<Int>
//           │   Int
//      Int  │   │ fun (Int).inc(): Int
//      │    │   │ │
    konst x1 = arr[0]++
//           fun (Int).inc(): Int
//           │ test3.arr: Array<Int>
//      Int  │ │   Int
//      │    │ │   │
    konst x2 = ++arr[1]
}

class Y(konst arr: Array<Int>)

fun test4(y: Y) {
//           test4.y: Y
//           │ konst (Y).arr: Array<Int>
//           │ │   Int
//      Int  │ │   │ fun (Int).inc(): Int
//      │    │ │   │ │
    konst x1 = y.arr[0]++
//           fun (Int).inc(): Int
//           │ test4.y: Y
//           │ │ konst (Y).arr: Array<Int>
//      Int  │ │ │   Int
//      │    │ │ │   │
    konst x2 = ++y.arr[1]
}
