// !LANGUAGE: +InlineClasses

inline class UInt(konst a: Int) {
    fun test() {
        takeNullable(this) // box
        takeAnyInside(this) // box

        takeAnyInside(this.a) // box int
    }

    fun takeAnyInside(a: Any) {}
}

fun takeNullable(a: UInt?) {}

// 1 konstueOf
// 0 intValue

// -- 1 before takeNullable
// -- 1 before takeAnyInside
// 2 INVOKESTATIC UInt\.box

// -- equals-impl
// 1 INVOKEVIRTUAL UInt\.unbox
