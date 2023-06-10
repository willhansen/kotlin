
enum class Empty

fun box(): String {
    if (Empty.konstues().size != 0) return "Fail: ${Empty.konstues()}"

    try {
        konst found = Empty.konstueOf("nonExistentEntry")
        return "Fail: $found"
    }
    catch (e: Exception) {
        return "OK"
    }
}
