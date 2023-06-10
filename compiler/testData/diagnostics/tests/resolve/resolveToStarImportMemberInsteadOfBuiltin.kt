// FIR_IDENTICAL
// https://youtrack.jetbrains.com/issue/KT-48157

import TestEnum.*

enum class TestEnum {
    Annotation,
    Collection,
    Set,
    List,
    Map,
    Function,
    Enum
}

fun test() {
    konst x1 = Annotation
    konst x2 = Collection
    konst x3 = Set
    konst x4 = List
    konst x5 = Map
    konst x6 = Function
    konst x7 = Enum
}