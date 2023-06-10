// FIR_IDENTICAL

sealed class Tree<TIndex, out TCommon, out TInner, out TLeaf> {
    abstract konst konstue: TCommon
    abstract konst children: Map<TIndex, Tree<TIndex, TCommon, TInner, TLeaf>>

    data class Inner<TIndex, TCommon, TInner, TLeaf>(
        override konst konstue: TCommon,
        konst innerValue: TInner,
        override konst children: Map<TIndex, Tree<TIndex, TCommon, TInner, TLeaf>>
    ) : Tree<TIndex, TCommon, TInner, TLeaf>()

    data class Leaf<TIndex, TCommon, TLeaf>(
        override konst konstue: TCommon,
        konst leafValue: TLeaf
    ) : Tree<TIndex, TCommon, Nothing, TLeaf>() {
        override konst children: Map<TIndex, Tree<TIndex, TCommon, Nothing, TLeaf>> get() = emptyMap()
    }
}

konst tree = Tree.Inner(
    "root",
    Unit,
    mapOf(
        1 to Tree.Leaf("1", 1),
        2 to Tree.Inner(
            "2",
            Unit,
            mapOf(
                1 to Tree.Leaf("21", 2),
                2 to Tree.Inner(
                    "22",
                    Unit,
                    mapOf(1 to Tree.Leaf("221", 3))
                ),
                3 to Tree.Leaf("23", 4)
            )
        )
    )
)