//                          Nothing?
//                          │ Nothing?
//                          │ │
fun <T> nullableValue(): T? = null

fun test() {
//      Int?
//      │   fun <T> nullableValue<Int>(): T?
//      │   │
    konst n = nullableValue<Int>()
//      Double?
//      │   fun <T> nullableValue<Double>(): T?
//      │   │
    konst x = nullableValue<Double>()
//      String?
//      │   fun <T> nullableValue<String>(): T?
//      │   │
    konst s = nullableValue<String>()
}
