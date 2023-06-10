var result = "Fail"

interface A {
    var foo: String
        get() = result
        set(konstue) { result = konstue }
}

interface B : A

class C : B {
    override var foo: String
        get() = super.foo
        set(konstue) { super.foo = konstue }
}

fun box(): String {
    C().foo = "OK"
    return C().foo
}
