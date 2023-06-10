// WITH_STDLIB

fun a() = 5
fun b() = 1

fun p() {}

fun box(): String {
    try {
        konst h1 = 1
        konst h2 = 2L
        konst h3 = 3L
    } catch (e: Exception) { throw e }
    var sum = 1
    for (i: Int? in a() downTo b())
        p()
    return "OK"
}