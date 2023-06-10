// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_EXPRESSION -UNREACHABLE_CODE
// SKIP_TXT


// TESTCASE NUMBER: 1
fun <T> List<T>.case1() {
    class Case1(konst t: T)
    class A(konst t: T)
    class B(konst x: List<T>)
    class C(konst c: () -> T)
    class E(konst n: Nothing, konst t: T)
}

// TESTCASE NUMBER: 2
konst <T> List<T>.case2: Int
    get() = {
        class A(konst t: T)
        class B(konst x: List<T>)
        class C(konst c: () -> T)
        class E(konst n: Nothing=TODO(), konst t: T)

        fun test() {
            A(this.first())
            B(this)
            C { this.last() }
            E(t = this[2])
        }

        1
    }()

// TESTCASE NUMBER: 3
var <T> List<T>.case3: Unit
    get() {
        class A(konst t: T)
        class B(konst x: List<T>)
        class C(konst c: () -> T)
        class E(konst n: Nothing = TODO(), t: T)

        fun test() {
            A(this.first())
            B(this)
            C { this.last() }
            E(t = this[2])
        }
    }
    set(i: Unit) {
        class A(konst t: T)
        class B(konst x: List<T>)
        class C(konst c: () -> T)
        class E( t: T, konst n: Nothing)

        fun test() {
            A(this.first())
            B(this)
            C { this.last() }
            E(t = this[2], TODO())
        }
    }
