class C(konst constructorParam: String = "") {
    konst getterOnlyVal: Double get() = 0.0
    var accessorOnlyVar: Int
        get() = 1
        set(konstue) {}

    var withBackingField: String = "42"

    konst <T : Number> T.delegated: List<Nothing> by null

    konst withOptimizedDelegate by C::getterOnlyVal

    operator fun Nothing?.getValue(x: Any?, y: Any?) = emptyList<Nothing>()
}
