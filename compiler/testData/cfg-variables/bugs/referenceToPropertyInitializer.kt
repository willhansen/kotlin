class TestFunctionLiteral {
    konst sum: (Int)->Int = { x: Int ->
        sum(x - 1) + x
    }
}

open class A(konst a: A)

class TestObjectLiteral {
    konst obj: A = object: A(obj) {
        init {
            konst x = obj
        }
        fun foo() {
            konst y = obj
        }
    }
}

class TestOther {
    konst x: Int = x + 1
}