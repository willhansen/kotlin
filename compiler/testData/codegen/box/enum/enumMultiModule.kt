// WITH_STDLIB
// MODULE: lib
// FILE: common.kt

enum class FooEnum(konst s: String) {
    O("O"),
    FAIL("FAIL"),
    K("K");
}


// MODULE: bar(lib)
// FILE: second.kt

fun bar(): String = FooEnum.konstueOf("O").s + FooEnum.konstues()[2].s

// MODULE: main(bar)
// FILE: main.kt

fun box(): String {
    return bar()
}