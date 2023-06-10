enum class MyEnum {
    O;
    companion object {
        konst K = "K"
    }
}

typealias MyAlias = MyEnum

fun box() = MyAlias.O.name + MyAlias.K