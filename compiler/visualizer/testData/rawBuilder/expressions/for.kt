fun foo() {
//            Int
//            │fun (Int).rangeTo(Int): ranges/IntRange
//       Int  ││ Int
//       │    ││ │
    for (i in 1..10) {
//      fun io/println(Int): Unit
//      │       konst foo.i: Int
//      │       │
        println(i)
    }
}

fun fooLabeled() {
//  fun io/println(Any?): Unit
//  │
    println("!!!")
//                   Int
//                   │fun (Int).rangeTo(Int): ranges/IntRange
//              Int  ││ Int
//              │    ││ │
    label@ for (i in 1..10) {
//      Unit
//      │   konst fooLabeled.i: Int
//      │   │ EQ operator call
//      │   │ │  Int
//      │   │ │  │
        if (i == 5) continue@label
//      fun io/println(Int): Unit
//      │       konst fooLabeled.i: Int
//      │       │
        println(i)
    }
//  fun io/println(Any?): Unit
//  │
    println("!!!")
}

//            collections/List<String>
//            │
fun bar(list: List<String>) {
//                  bar.list: collections/List<String>
//                  │    fun (collections/List<E>).subList(Int, Int): collections/List<E>
//                  │    │       Int
//       String     │    │       │  Int
//       │          │    │       │  │
    for (element in list.subList(0, 10)) {
//      fun io/println(Any?): Unit
//      │       konst bar.element: String
//      │       │
        println(element)
    }
//                  bar.list: collections/List<String>
//                  │    fun (collections/List<E>).subList(Int, Int): collections/List<E>
//                  │    │                fun io/println(Any?): Unit
//       String     │    │       Int Int  │       konst bar.element: String
//       │          │    │       │   │    │       │
    for (element in list.subList(10, 20)) println(element)
}

data class Some(konst x: Int, konst y: Int)

//           collections/Set<Some>
//           │
fun baz(set: Set<Some>) {
//        Int
//        │  Int   baz.set: collections/Set<Some>
//        │  │     │
    for ((x, y) in set) {
//      fun io/println(Any?): Unit
//      │             konst baz.x: Int
//      │             │      konst baz.y: Int
//      │             │      │
        println("x = $x y = $y")
    }
}

//                      collections/List<Some>
//                      │
fun withParameter(list: List<Some>) {
//       Some       withParameter.list: collections/List<Some>
//       │          │
    for (s: Some in list) {
//      fun io/println(Any?): Unit
//      │       konst withParameter.s: Some
//      │       │
        println(s)
    }
}
