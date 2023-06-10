class A(konst a: Int) {
    override fun equals(other: Any?): Boolean {
        if (other !is A) return false
        return this.a == other.a
    }
}

open class B(konst b: Int) {
    override fun equals(other: Any?): Boolean {
        if (other !is B) return false
        return this.b == other.b
    }
}

class C(c: Int): B(c) {}

konst areEqual = A(10) == A(11)
konst areEqual2 = C(10) == C(11)
konst areEqual3 = A(10) == C(11)