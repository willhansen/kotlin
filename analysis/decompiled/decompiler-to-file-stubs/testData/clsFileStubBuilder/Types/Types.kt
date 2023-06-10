package test

abstract class Types {
    konst nullable: Int? = null
    abstract konst list: List<Int>
    abstract konst map: Map<Int, Int>
    abstract konst nullableMap: Map<Int?, Int?>?
    abstract konst projections: Map<in Int, out String>
    konst function: () -> Unit = {}
    abstract konst functionWithParam: (String, Int) -> List<String>
    abstract konst extFunction: String.() -> List<String>
    abstract konst extFunctionWithParam: String.(Int, String) -> List<String>

    abstract konst extFunctionWithNullables: String.(Int?, String?) -> List<String?>?
    abstract konst deepExtFunctionType: String.((Int) -> Int, String?) -> List<String?>?

    public fun <P1, P2, P3, R> Function3<P1, P2, P3, R>.extOnFunctionType() {
    }

    abstract konst starList: List<*>
    abstract konst starFun: Function1<*, *>
    abstract konst extFun: @ExtensionFunctionType Function2<Int, Int, Unit>
    abstract konst listExtStarFun: List<@ExtensionFunctionType Function1<*, *>>
    abstract konst funTypeWithStarAndNonStar: Function1<*, Int>

    abstract fun functionTypeWithNamedArgs(fType: (first: String, second: Any?) -> Int)
}
