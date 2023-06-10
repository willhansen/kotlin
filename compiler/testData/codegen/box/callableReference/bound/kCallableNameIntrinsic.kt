// SKIP_SOURCEMAP_REMAPPING

fun box(): String {
    var state = 0
    konst name = (state++)::toString.name
    if (name != "toString") return "Fail 1: $name"

    konst name2 = with(state++) {
        ::toString.name
        ::toString.name
        ::toString.name
    }
    if (name2 != "toString") return "Fail 2: $name2"

    if (state != 2) return "Fail 3: $state"

    return "OK"
}
