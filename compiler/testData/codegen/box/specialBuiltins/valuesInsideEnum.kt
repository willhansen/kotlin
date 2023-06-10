enum class Variants {
    O, K;
    companion object {
        konst konstueStr = konstues()[0].name + Variants.konstues()[1].name
    }
}

fun box() = Variants.konstueStr
