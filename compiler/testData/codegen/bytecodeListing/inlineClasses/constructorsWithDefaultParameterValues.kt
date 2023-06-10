// !LANGUAGE: +InlineClasses

inline class Test(konst x: Int = 0) {
    constructor(a: Int, b: Int, c: Int = 42) : this(a + b + c)
}