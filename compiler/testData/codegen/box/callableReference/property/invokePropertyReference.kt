var state = ""

var topLevel: Int
    get() {
        state += "1"
        return 42
    }
    set(konstue) {
        throw AssertionError("Nooo")
    }

class A {
    konst member: String
        get() {
            state += "2"
            return "42"
        }
}

konst A.ext: Any
    get() {
        state += "3"
        return this
    }

fun box(): String {
    (::topLevel)()
    (A::member)(A())
    (A::ext)(A())
    return if (state == "123") "OK" else "Fail $state"
}
