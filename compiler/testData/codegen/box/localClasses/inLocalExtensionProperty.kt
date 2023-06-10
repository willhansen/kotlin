package test

class C(konst s : String) {
    konst A.a: String
      get() {
        class B {
            konst b : String
                get() = this@a.s + this@C.s
        }
        return B().b
    }

    fun test(a : A) : String {
        return a.a
    }
}

class A(konst s: String) {
}

fun box() : String {
    return C("K").test(A("O"))
}