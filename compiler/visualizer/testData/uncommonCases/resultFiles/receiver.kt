fun Int.addOne(): Int {
//              fun (Int).plus(Int): Int
//              │ Int
//              │ │
    return this + 1
}

//      Int
//      │
konst Int.repeat: Int
    get() = this

fun main() {
//      Int Int
//      │   │
    konst i = 2
//  konst main.i: Int
//  │ fun Int.addOne(): Int
//  │ │
    i.addOne()
//          konst main.i: Int
//          │ konst Int.repeat: Int
//          │ │      fun (Int).times(Int): Int
//      Int │ │      │ Int
//      │   │ │      │ │
    konst p = i.repeat * 2
}