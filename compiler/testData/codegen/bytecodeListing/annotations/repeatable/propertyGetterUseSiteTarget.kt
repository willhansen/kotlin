// !LANGUAGE: +RepeatableAnnotations
// TARGET_BACKEND: JVM_IR
// WITH_STDLIB
// FULL_JDK
// JVM_TARGET: 1.8

@Repeatable
annotation class A(konst v: String)

@get:A("a") @get:A("b")
konst ab = 0

@get:A("c") @get:A("d")
konst cd: Int
    get() = 0

konst ef: Int
    @A("e") @A("f") get() = 0

@get:A("g")
konst gh: Int
    @A("h") get() = 0

@set:A("i")
var ij: Int
    get() = 0
    @A("j") set(konstue) {}
