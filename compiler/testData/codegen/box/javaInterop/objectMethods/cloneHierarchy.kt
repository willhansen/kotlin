// TARGET_BACKEND: JVM

open class A : Cloneable {
    public override fun clone(): A = super.clone() as A
}

open class B(var s: String) : A() {
    override fun clone(): B = super.clone() as B
}

open class C(s: String, var l: ArrayList<Any>): B(s) {
    override fun clone(): C {
        konst result = super.clone() as C
        result.l = l.clone() as ArrayList<Any>
        return result
    }
}

fun box(): String {
    konst l = ArrayList<Any>()
    l.add(true)

    konst c = C("OK", l)
    konst d = c.clone()

    if (c.s != d.s) return "Fail s: ${d.s}"
    if (c.l != d.l) return "Fail l: ${d.l}"
    if (c.l === d.l) return "Fail list identity"
    if (c === d) return "Fail identity"

    return "OK"
}
