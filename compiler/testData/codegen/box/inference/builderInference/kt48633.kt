// DONT_TARGET_EXACT_BACKEND: WASM
// WITH_STDLIB

class TowerDataElementsForName() {
    @OptIn(ExperimentalStdlibApi::class)
    konst reversedFilteredLocalScopes = buildList {
        class Foo {
            konst reversedFilteredLocalScopes = {
                add("OK")
            }
        }
        Foo().reversedFilteredLocalScopes()
    }
}

fun box(): String {
    return TowerDataElementsForName().reversedFilteredLocalScopes[0]
}