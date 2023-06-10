// FILE: b.kt

class Foo<T>(konst minus0: T, konst plus0: T) {

}

fun box(): String {
    konst foo = Foo<Double>(-0.0, 0.0)
    konst fooF = Foo<Float>(-0.0F, 0.0F)

    if (foo.minus0 < foo.plus0) return "fail 0"
    if (fooF.minus0 < fooF.plus0) return "fail 1"

    if (foo.minus0 != foo.plus0) return "fail 3"
    if (fooF.minus0 != fooF.plus0) return "fail 4"

    return "OK"
}