// !DIAGNOSTICS: -UNUSED_VARIABLE -ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE -UNUSED_VALUE -UNUSED_PARAMETER -UNUSED_EXPRESSION
// SKIP_TXT

// TESTCASE NUMBER: 1

fun case1(){
    OtherClass().zoooo()
    checkSubtype<MainClass.Base2>(OtherClass().zoooo())
}

class MainClass {
    abstract class Base1() {
        abstract konst a: CharSequence
        abstract var b: CharSequence

        abstract fun foo(): CharSequence
    }

    abstract class Base2 : Base1() {
        abstract fun boo(x: Int = 10)
    }

}

class OtherClass {

    abstract inner class ImplBase2() : MainClass.Base2() {
        override var b: CharSequence
            get() = TODO()
            set(konstue) {}
        override konst a: CharSequence = ""

        override fun boo(x: Int) {
            TODO()
        }

    }

    fun zoooo(): ImplBase2 {
        konst k = object : ImplBase2() {
            override fun foo(): CharSequence = ""
        }
        return k
    }
}
