// !LANGUAGE: +UnrestrictedBuilderInference +ForbidInferringPostponedTypeVariableIntoDeclaredUpperBound
// ISSUE: KT-56169

internal class TowerDataElementsForName() {
    konst reversedFilteredLocalScopes by lazy(LazyThreadSafetyMode.NONE) {
        @OptIn(ExperimentalStdlibApi::class)
        buildList {
            for (i in lastIndex downTo 0) {
                add("")
            }
        }
    }
}

internal class TowerDataElementsForName2() {
    @OptIn(ExperimentalStdlibApi::class)
    konst reversedFilteredLocalScopes = buildList {
        konst reversedFilteredLocalScopes by lazy(LazyThreadSafetyMode.NONE) {
            @OptIn(ExperimentalStdlibApi::class)
            buildList {
                for (i in lastIndex downTo 0) {
                    add("")
                }
            }
        }
        add(reversedFilteredLocalScopes)
    }
}

internal class TowerDataElementsForName3() {
    konst reversedFilteredLocalScopes by <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER, NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>lazy<!>(LazyThreadSafetyMode.NONE) {
        @OptIn(ExperimentalStdlibApi::class)
        buildList l1@ {
            for (i in lastIndex downTo 0) {
                konst reversedFilteredLocalScopes by lazy(LazyThreadSafetyMode.NONE) {
                    @OptIn(ExperimentalStdlibApi::class)
                    buildList {
                        for (i in lastIndex downTo 0) {
                            add("")
                            this@l1.add("")
                        }
                    }
                }
            }
        }
    }
}

internal class TowerDataElementsForName4() {
    @OptIn(ExperimentalStdlibApi::class)
    konst reversedFilteredLocalScopes = <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>buildList<!> l1@ {
        class Foo {
            konst reversedFilteredLocalScopes by lazy(LazyThreadSafetyMode.NONE) {
                @OptIn(ExperimentalStdlibApi::class)
                buildList {
                    for (i in lastIndex downTo 0) {
                        add("")
                        this@l1.add("")
                    }
                }
            }
        }
    }
}
