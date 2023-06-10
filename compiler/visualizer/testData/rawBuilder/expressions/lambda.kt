// FIR_IGNORE
// WITH_STDLIB
data class Tuple(konst x: Int, konst y: Int)

//                                Int
//                                │ fun ((P1) -> R).invoke(P1): R
//                                │ │ constructor Tuple(Int, Int)
//                                │ │ │     Int
//                                │ │ │     │  Int
//                                │ │ │     │  │
inline fun use(f: (Tuple) -> Int) = f(Tuple(1, 2))

fun foo(): Int {
//      (Tuple) -> Int
//      │
    konst l1 = { t: Tuple ->
//              foo.<anonymous>.t: Tuple
//          Int │ konst (Tuple).x: Int
//          │   │ │
        konst x = t.x
//              foo.<anonymous>.t: Tuple
//          Int │ konst (Tuple).y: Int
//          │   │ │
        konst y = t.y
//      konst foo.<anonymous>.x: Int
//      │ fun (Int).plus(Int): Int
//      │ │ konst foo.<anonymous>.y: Int
//      │ │ │
        x + y
    }
//  fun use((Tuple) -> Int): Int
//  │               konst foo.<anonymous>.x: Int
//  │      Int      │ fun (Int).plus(Int): Int
//  │      │  Int   │ │ konst foo.<anonymous>.y: Int
//  │      │  │     │ │ │
    use { (x, y) -> x + y }

//         fun use((Tuple) -> Int): Int
//         │
    return use {
//      Unit
//      │   foo.<anonymous>.it: Tuple
//      │   │  konst (Tuple).x: Int
//      │   │  │ EQ operator call
//      │   │  │ │  Int           Int
//      │   │  │ │  │             │
        if (it.x == 0) return@foo 0
//                 foo.<anonymous>.it: Tuple
//                 │  konst (Tuple).y: Int
//                 │  │
        return@use it.y
    }
}

fun bar(): Int {
//         fun use((Tuple) -> Int): Int
//         │
    return use lambda@{
//      Unit
//      │   bar.<anonymous>.it: Tuple
//      │   │  konst (Tuple).x: Int
//      │   │  │ EQ operator call
//      │   │  │ │  Int           Int
//      │   │  │ │  │             │
        if (it.x == 0) return@bar 0
//                    bar.<anonymous>.it: Tuple
//                    │  konst (Tuple).y: Int
//                    │  │
        return@lambda it.y
    }
}

//             collections/List<Int>
//             │
fun test(list: List<Int>) {
//      collections/MutableMap<Int, String>
//      │     fun <K, V> collections/mutableMapOf<Int, String>(): collections/MutableMap<K, V>
//      │     │
    konst map = mutableMapOf<Int, String>()
//  test.list: collections/List<Int>
//  │    fun <T> collections/Iterable<T>.forEach<Int>((T) -> Unit): Unit
//  │    │         konst test.map: collections/MutableMap<Int, String>
//  │    │         │   fun <K, V> collections/MutableMap<K, V>.getOrPut<Int, String>(K, () -> V): V
//  │    │         │   │        test.<anonymous>.it: Int
//  │    │         │   │        │     fun <T> collections/mutableListOf<???>(): collections/MutableList<T>
//  │    │         │   │        │     │                  fun (String).plus(Any?): String
//  │    │         │   │        │     │                  │
    list.forEach { map.getOrPut(it, { mutableListOf() }) += "" }
}

//  () -> Unit
//  │
konst simple = { }

//  () -> Int   Int
//  │           │
konst another = { 42 }
