enum class MyEnum {
    First,
    Second
}

fun getValue() = MyEnum.First

var result = "Failed"

fun getLambda(): (Int) -> Unit =
    when (konst konstue = getValue()) {
        MyEnum.Second -> { _ -> }
        MyEnum.First -> { _ ->
            if (konstue == MyEnum.First) {
                result = "OK"
            }
        }
    }

fun box(): String {
    getLambda().invoke(2)
    return result
}
