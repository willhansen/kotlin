// p.A

package p

class A {
    init {
        fun localFunInInit() {}
    }

    constructor(x: Int) {
        fun localFunInConstructor() {}
    }

    fun memberFun() {
        fun localFunInMemberFun() {}
    }

    konst property: Int
        get() {
            fun localFunInPropertyAccessor() {}
            return 1
        }
}

