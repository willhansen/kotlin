// FIR_IDENTICAL
// !LANGUAGE: +ContextReceivers

class A<T>(konst a: T)
class B(konst b: Any)
class C(konst c: Any)

context(labelAInt@A<Int>, A<String>, labelB@B) fun f() {
    this@labelAInt.a.toFloat()
    this@A.a.length
    this@labelB.b
}

context(labelAInt@A<Int>, A<String>, labelB@B) konst C.p: Int
    get() {
        this@labelAInt.a.toFloat()
        this@A.a.length
        this@labelB.b
        this@C.c
        this@p.c
        this.c
        return 1
    }
