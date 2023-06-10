// !DUMP_CFG
interface B

interface A {
    konst b: B?
}

class C(a: A, b: B) {
    init {
        konst c = a.b?.let {
            C(a, it)
        }
    }
}
