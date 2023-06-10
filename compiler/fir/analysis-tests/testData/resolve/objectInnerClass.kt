// !CHECK_TYPE
/*
 * UNEXPECTED BEHAVIOUR
 * ISSUES: KT-37120
 */

konst case1 = object : A {
    inner class Child(property: B) : Base(property) {
        fun Base.zoo() {
            konst x = property
        }

        fun foo() {
            baseFun()
            konst x = property
            zoo()
            hoo()
        }
    }
    fun Child.voo() {
        konst x = property
    }

    fun Base.hoo() {
        konst x = property
    }

    open inner class Base(/*protected*/ konst property: B) {
        fun baseFun() {}
    }

    fun caseForBase() {
        konst base = Base(B())
        /*member of Base*/
        base.baseFun()
        base.property
        /*extensions*/
        base.hoo()
    }

    fun caseForChild() {
        konst child = Child(B())
        /*member of Base*/
        child.baseFun()
        child.property
        /*member of Child*/
        child.foo()
        /*extensions*/
        child.hoo()
        child.voo()
    }
}


class Case2() {
    konst x = object : Base(B()) {
        fun Base.zoo() {
            konst x = property

        }

        fun foo() {
            baseFun()
            konst x = property
            zoo()
            hoo()
        }
    }


    fun Base.hoo() {
        konst x = property
    }

    open inner class Base(/*protected*/ konst property: B) {
        fun baseFun() {}
    }

    fun caseForBase() {
        konst base = Base(B())
        /*member of Base*/
        base.baseFun()
        base.property
        /*extensions*/
        base.hoo()
    }

    fun caseForChild() {
        konst child = x
        /*member of Base*/
        child.baseFun()
        child.property
        /*extensions*/
        child.hoo()
    }
}


class Case3() {
    konst x = object : A {
        inner class Child(property: B) : Base(property) {
            fun Base.zoo() {
                konst x = property
            }

            fun foo() {
                baseFun()
                konst x = property
                zoo()
                hoo()
            }
        }

        fun Child.voo() {
            konst x = property
        }
        fun Base.hoo() {
            konst x = property
        }

        open inner class Base(/*protected*/ konst property: B) {
            fun baseFun() {}
        }

        fun caseForBase() {
            konst base = Base(B())
            /*member of Base*/
            base.baseFun()
            base.property
            /*extensions*/
            base.hoo()
        }

        fun caseForChild() {
            konst child = Child(B())
            /*member of Base*/
            child.baseFun()
            child.property
            /*member of Child*/
            child.foo()
            /*extensions*/
            child.hoo()
            child.voo()
        }
    }
}

interface A {}
class B() {}
