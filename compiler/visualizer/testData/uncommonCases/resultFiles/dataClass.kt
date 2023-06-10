// FIR_IGNORE
data class Vector(konst x: Int, konst y: Int) {
//                                  Vector
//                                  │ constructor Vector(Int, Int)
//                                  │ │      konst (Vector).x: Int
//                                  │ │      │ fun (Int).plus(Int): Int
//                                  │ │      │ │ Vector.plus.other: Vector
//                                  │ │      │ │ │     konst (Vector).x: Int
//                                  │ │      │ │ │     │  konst (Vector).y: Int
//                                  │ │      │ │ │     │  │ fun (Int).plus(Int): Int
//                                  │ │      │ │ │     │  │ │ Vector.plus.other: Vector
//                                  │ │      │ │ │     │  │ │ │     konst (Vector).y: Int
//                                  │ │      │ │ │     │  │ │ │     │
    fun plus(other: Vector): Vector = Vector(x + other.x, y + other.y)
}

fun main() {
//      Vector
//      │   constructor Vector(Int, Int)
//      │   │      Int
//      │   │      │  Int
//      │   │      │  │
    konst a = Vector(1, 2)
//      Vector
//      │   constructor Vector(Int, Int)
//      │   │       Int
//      │   │       │  Int
//      │   │       │  │
    konst b = Vector(-1, 10)

//  fun io/println(Any?): Unit
//  │             konst main.a: Vector
//  │             │        konst main.b: Vector
//  │             │        │ fun (Vector).toString(): String
//  │             │        │ │
    println("a = $a, b = ${b.toString()}")
//  fun io/println(Any?): Unit
//  │                  fun (String).plus(Any?): String
//  │                  │  konst main.a: Vector
//  │                  │  │ fun (Vector).plus(Vector): Vector
//  │                  │  │ │ konst main.b: Vector
//  │                  │  │ │ │
    println("a + b = " + (a + b))
//  fun io/println(Any?): Unit
//  │                   konst main.a: Vector
//  │                   │ fun (Vector).hashCode(): Int
//  │                   │ │
    println("a hash - ${a.hashCode()}")

//                             konst main.a: Vector
//                             │ fun (Vector).equals(Any?): Boolean
//  fun io/println(Any?): Unit │ │      konst main.b: Vector
//  │                          │ │      │
    println("a is equal to b ${a.equals(b)}")
}
