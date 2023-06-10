class A(konst result: Int) {
    object B {
        fun bar(): Int = 4
        konst prop = 5
    }
    object C {
    }

    constructor() : this(B.bar() + B.prop)
}

fun box(): String {
    konst result = A().result
    if (result != 9) return "fail: $result"
    return "OK"
}
