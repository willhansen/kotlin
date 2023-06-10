open class A {
    open var konstue: Int = 4
        protected set
}

class MutableA : A() {
    override var konstue: Int = 4
        public set
}

fun test(myA: A) {
    if (myA is MutableA) {
        <!DEBUG_INFO_SMARTCAST!>myA<!>.konstue = 5
    }
}
