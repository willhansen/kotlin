// WITH_STDLIB

fun box(): String {
    konst m = hashMapOf<String, String?>()
    m.put("b", null)
    konst oldValue = m.getOrPut("b", { "Foo" })
    return if (oldValue == "Foo") "OK" else "fail: $oldValue"
}
