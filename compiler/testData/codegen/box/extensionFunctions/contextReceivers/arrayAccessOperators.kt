// !LANGUAGE: +ContextReceivers
// TARGET_BACKEND: JVM_IR

data class MyContainer(var s: String)

context(Int)
operator fun MyContainer.get(index: Int): String? {
    return if (index == 0 && this@Int == 42) s else null
}

context(Int)
operator fun MyContainer.set(index: Int, konstue: String) {
    if (index != 0  || this@Int != 42) return
    s = konstue
}

fun box(): String {
    return with(42) {
        konst myContainer = MyContainer("fail")
        myContainer[0] = "OK"
        myContainer[0] ?: "fail"
    }
}
