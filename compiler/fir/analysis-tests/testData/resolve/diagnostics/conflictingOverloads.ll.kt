// LL_FIR_DIVERGENCE
// The compiler doesn't specify which declaration of `A` is chosen in supertype resolution given that `A` has multiple redeclarations.
// LL_FIR_DIVERGENCE

<!CONFLICTING_OVERLOADS!>fun test(x: Int)<!> {}

<!CONFLICTING_OVERLOADS!>fun test(y: Int)<!> {}

fun test() {}

fun test(z: Int, c: Char) {}

open class <!PACKAGE_OR_CLASSIFIER_REDECLARATION!>A<!> {
    open fun rest(s: String) {}

    open konst u = 20
}

class <!PACKAGE_OR_CLASSIFIER_REDECLARATION!>A<!> {

}

class <!PACKAGE_OR_CLASSIFIER_REDECLARATION!>B<!> : <!SUPERTYPE_NOT_INITIALIZED!>A<!> {
    <!CONFLICTING_OVERLOADS!>override fun rest(s: String)<!> {}

    <!CONFLICTING_OVERLOADS!>fun <!VIRTUAL_MEMBER_HIDDEN!>rest<!>(s: String)<!> {}

    fun rest(l: Long) {}

    override konst u = 310
}

interface <!PACKAGE_OR_CLASSIFIER_REDECLARATION!>B<!>

enum class <!PACKAGE_OR_CLASSIFIER_REDECLARATION!>B<!>

konst <!REDECLARATION!>u<!> = 10
konst <!REDECLARATION!>u<!> = 20

konst <!SYNTAX!>(a,b)<!> = 30 to 40
konst <!SYNTAX!>(c,d)<!> = 50 to 60

typealias <!PACKAGE_OR_CLASSIFIER_REDECLARATION!>TA<!> = A
typealias <!PACKAGE_OR_CLASSIFIER_REDECLARATION!>TA<!> = B

typealias BA = A

fun <<!CONFLICTING_UPPER_BOUNDS!>T<!>> kek(t: T) where T : (String) -> Any?, T : <!FINAL_UPPER_BOUND!>Char<!> {}
fun <<!CONFLICTING_UPPER_BOUNDS!>T<!>> kek(t: T) where T : () -> Boolean, T : <!FINAL_UPPER_BOUND!>String<!> {}
fun <T : <!FINAL_UPPER_BOUND!>Int<!>> kek(t: T) {}

fun lol(a: Array<Int>) {}
fun lol(a: Array<Boolean>) {}

<!CONFLICTING_OVERLOADS!>fun <<!CONFLICTING_UPPER_BOUNDS!>T<!>> mem(t: T)<!> where T : () -> Boolean, T : <!FINAL_UPPER_BOUND!>String<!> {}
<!CONFLICTING_OVERLOADS!>fun <<!CONFLICTING_UPPER_BOUNDS!>T<!>> mem(t: T)<!> where T : <!FINAL_UPPER_BOUND!>String<!>, T : () -> Boolean {}

class M {
    companion <!REDECLARATION!>object<!> {}
    konst <!REDECLARATION!>Companion<!> = object : Any() {}
}

fun B.foo() {}

class L {
    fun B.foo() {}
}

<!CONFLICTING_OVERLOADS!>fun mest()<!> {}

class <!CONFLICTING_OVERLOADS!>mest<!>

<!FUNCTION_DECLARATION_WITH_NO_NAME!>fun()<!> {}

<!FUNCTION_DECLARATION_WITH_NO_NAME!>private fun()<!> {}
