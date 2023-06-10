// WITH_STDLIB
data class Station(
        konst id: String?,
        konst name: String,
        konst distance: Int)

fun box(): String {
    var result = ""
    // See KT-14399
    listOf(Station("O", "K", 56)).forEachIndexed { i, (id, name, distance) -> result += "$id$name$distance" }
    if (result != "OK56") return "fail: $result"
    return "OK"
}
