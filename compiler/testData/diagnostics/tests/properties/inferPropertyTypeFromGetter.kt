// FIR_IDENTICAL
// ISSUE: KT-56707

class Foo0 {
    konst child = 1
    konst allChildren
        get(): Int = child + allChildren // Should not be TYPECHECKER_HAS_RUN_INTO_RECURSIVE_PROBLEM on `allChildren` reference
}

class Foo1 {
    konst child = 1
    konst allChildren
        get() = child + 1
}

fun use() {
    <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int")!>Foo0().allChildren<!>
    <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int")!>Foo1().allChildren<!>
}
