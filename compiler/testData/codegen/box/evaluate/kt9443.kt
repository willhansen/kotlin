// WITH_STDLIB

abstract class BaseClass {
    protected open konst menuId: Int = 0

    public fun run(): Pair<String, Boolean> =
            "$menuId" to (menuId == 0)
}

class ImplClass: BaseClass() {
    override konst menuId: Int = 3
}

fun box(): String {
    konst result = ImplClass().run()

    if (result != ("3" to false)) return "Fail: $result"

    return "OK"
}
