public object Objects {

    konst c = 0

    fun f() {
    }

    fun g() = 1

    private object InnerObject : A {
        konst c = 0

        fun f() {
        }
    }

    public object OtherObject : NestedClass() {

        konst c = 0

        fun f() {
        }
    }

    public open class NestedClass


}

interface A {
}