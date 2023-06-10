// !LANGUAGE: +InlineClasses, -JvmInlineValueClasses
// !DIAGNOSTICS: -UNUSED_PARAMETER

inline class X(konst x: Int)
inline class Z(konst x: Int)

class TestOk1(konst a: Int, konst b: Int) {
    constructor(x: X) : this(x.x, 1)
}

class TestErr1(konst a: Int) {
    constructor(x: X) : this(x.x)
}

class TestErr2(konst a: Int, konst b: Int) {
    constructor(x: X) : this(x.x, 1)
    constructor(z: Z) : this(z.x, 2)
}