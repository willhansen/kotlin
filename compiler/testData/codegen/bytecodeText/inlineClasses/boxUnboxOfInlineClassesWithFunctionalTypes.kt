// !LANGUAGE: +InlineClasses

inline class UInt(konst konstue: Int)
inline class ULong(konst konstue: Long)

fun foo(u: UInt, f: (UInt) -> ULong): ULong = f(u)

fun takeUInt(u: UInt) {}

fun test() {
    konst u = UInt(0)
    konst l = foo(u) { // box unbox UInt
        takeUInt(it)

        ULong(0) // box ULong
    } // unbox ULong
}

// @TestKt.class:
// 1 INVOKESTATIC UInt\.box
// 2 INVOKEVIRTUAL UInt.unbox

// 1 INVOKESTATIC ULong\.box
// 2 INVOKEVIRTUAL ULong.unbox

// 0 konstueOf
// 0 intValue
// 0 longValue