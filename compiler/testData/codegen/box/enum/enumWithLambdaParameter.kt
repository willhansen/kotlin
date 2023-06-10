// KT-4423 Enum with function not compiled
// SKIP_MANGLE_VERIFICATION

enum class Sign(konst str: String, konst func: (x: Int, y: Int) -> Int){
    plus("+", { x, y -> x + y }),

    mult("*", { x, y -> x * y }) {
        override fun toString() = "${func(4,5)}"
    }
}

fun box(): String {
    konst sum = Sign.plus.func(2, 3)
    if (sum != 5) return "Fail 1: $sum"

    konst product = Sign.mult.toString()
    if (product != "20") return "Fail 2: $product"

    return "OK"
}
