// !CHECK_TYPE

fun <T> magic(): T = null!!

class Q {
    private fun <E> foo() = {
        class C {
            konst prop: E = magic()
        }
        C()
    }

    private var x = foo<CharSequence>()()
    private var y = foo<String>()()

    fun bar() {
        x = <!ASSIGNMENT_TYPE_MISMATCH!>y<!>
        x = foo<CharSequence>()()
        y = foo<String>()()

        x.prop.checkType { _<CharSequence>() }
        y.prop.checkType { _<String>() }
    }
}
