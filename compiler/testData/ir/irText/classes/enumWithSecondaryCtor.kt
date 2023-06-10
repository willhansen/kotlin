// WITH_STDLIB
// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6

// MUTE_SIGNATURE_COMPARISON_K2: ANY
// ^ KT-57775

enum class Test0(konst x: Int) {
    ZERO;
    constructor() : this(0)
}

enum class Test1(konst x: Int) {
    ZERO, ONE(1);
    constructor() : this(0)
}

enum class Test2(konst x: Int) {
    ZERO {
        override fun foo() {
            println("ZERO")
        }
    },
    ONE(1) {
        override fun foo() {
            println("ONE")
        }
    };
    constructor() : this(0)
    abstract fun foo()
}
