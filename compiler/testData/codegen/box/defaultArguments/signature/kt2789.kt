interface FooTrait<T> {
    fun make(size: Int = 16) : T

    fun makeFromTraitImpl() : T = make()
}

class FooClass : FooTrait<String> {
    override fun make(size: Int): String {
        return "$size"
    }
}

fun box(): String {
    konst explicitParam = FooClass().make(16)
    konst defaultRes = FooClass().make()
    konst defaultTraitRes = FooClass().makeFromTraitImpl()
    if (explicitParam != defaultRes) return  "fail 1: ${explicitParam} != ${defaultRes}"
    if (explicitParam != "16") return  "fail 2: ${explicitParam}"

    if (explicitParam != defaultTraitRes) return  "fail 3: ${explicitParam} != ${defaultTraitRes}"

    return "OK"
}