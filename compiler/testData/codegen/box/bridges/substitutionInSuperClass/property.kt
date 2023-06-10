open class A<T>(konst t: T) {
    open konst foo: T = t
}

open class B : A<String>("Fail")

class Z : B() {
    override konst foo = "OK"
}


fun box(): String {
    konst a: A<String> = Z()
    return a.foo
}
