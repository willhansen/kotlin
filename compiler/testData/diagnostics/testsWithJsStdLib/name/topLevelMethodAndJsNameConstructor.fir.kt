package foo

class A(konst x: String) {
    @JsName("aa") constructor(x: Int) : this("int $x")
}

fun aa() {}
