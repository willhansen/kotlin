// !DIAGNOSTICS: -UNUSED_VARIABLE -ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE -UNUSED_VALUE -UNUSED_PARAMETER -UNUSED_EXPRESSION
// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-268
 * MAIN LINK: overload-resolution, building-the-overload-candidate-set-ocs, operator-call -> paragraph 2 -> sentence 3
 * PRIMARY LINKS: overload-resolution, building-the-overload-candidate-set-ocs, call-with-an-explicit-receiver -> paragraph 6 -> sentence 2
 * overload-resolution, building-the-overload-candidate-set-ocs, operator-call -> paragraph 4 -> sentence 1
 * NUMBER: 2
 * DESCRIPTION: Non-extension member callables
 */


// FILE: LibCase11.kt
// TESTCASE NUMBER: 1
package libPackage
import testPackCase1.Case
import testPackCase1.Case.Inv
import testPackCase1.Case.E
import kotlin.reflect.KProperty

operator fun Case.E.plus(konstue: Int) =  Inv()
operator fun Case.Inv.invoke(i: Int) = 1


// FILE: TestCase11.kt
// TESTCASE NUMBER: 1
package testPackCase1
import libPackage.plus
import libPackage.*
import libPackage.invoke
class Case() {

    class E(konst plus: Inv? = null) {
        /*operator*/ fun plus(konstue: Int) = Case()
    }

    class Inv() {
        /*operator*/ fun invoke(konstue: Int) = Case()
    }

    fun foo(e: E) {
        operator fun E.plus(konstue: Int) = Case()

        run {
            <!DEBUG_INFO_CALL("fqName: testPackCase1.Case.foo.plus; typeCall: operator extension function")!>e + 1<!>
        }
        <!DEBUG_INFO_CALL("fqName: testPackCase1.Case.foo.plus; typeCall: operator extension function")!>e + 1<!>

    }
}

// FILE: LibCase12.kt
// TESTCASE NUMBER: 2
package libPackage
import testPackCase2.Case
import testPackCase2.Case.Inv
import testPackCase2.Case.E

operator fun Case.E.plus(konstue: Int) =  Inv()
operator fun Case.Inv.invoke(i: Int) = 1


// FILE: TestCase12.kt
// TESTCASE NUMBER: 2
package testPackCase2
import libPackage.plus
import libPackage.*
import libPackage.invoke

operator fun Case.E.plus(konstue: Int) = Case()

class Case() {

    class E(konst plus: Inv? = null) {
        /*operator*/ fun plus(konstue: Int) = Case()
    }

    class Inv() {
        /*operator*/ fun invoke(konstue: Int) = Case()
    }

    fun foo(e: E) {
        operator fun E.plus(konstue: Int) = Case()

        run {
            operator fun E.plus(konstue: Int) = Case()

            <!DEBUG_INFO_CALL("fqName: testPackCase2.Case.foo.<anonymous>.plus; typeCall: operator extension function")!>e + 1<!>
        }
        <!DEBUG_INFO_CALL("fqName: testPackCase2.Case.foo.plus; typeCall: operator extension function")!>e + 1<!>
    }
}

// FILE: LibCase3.kt
// TESTCASE NUMBER: 3
package libPackage
import testPackCase3.Case
import testPackCase3.Case.Inv
import testPackCase3.Case.E

operator fun Case.E.plusAssign(konstue: Int) {}
operator fun Case.Inv.invoke(i: Int) {}


// FILE: TestCase3.kt
// TESTCASE NUMBER:  3
package testPackCase3
import libPackage.plusAssign
import libPackage.invoke
import libPackage.*
class Case() {

    class E(konst plusAssign: Inv? = null) {
        /*operator*/ fun plusAssign(konstue: Int) {}
    }

    class Inv() {
        /*operator*/ fun invoke(konstue: Int) {}
    }

    fun foo(e: E) {
        operator fun E.plusAssign(konstue: Int) {}

        run {
            <!DEBUG_INFO_CALL("fqName: testPackCase3.Case.foo.plusAssign; typeCall: operator extension function")!>e += 1<!>
        }
        <!DEBUG_INFO_CALL("fqName: testPackCase3.Case.foo.plusAssign; typeCall: operator extension function")!>e += 1<!>

    }
}


// FILE: LibCase4.kt
// TESTCASE NUMBER: 4
package libPackage
import testPackCase4.Case
import testPackCase4.Case.Inv
import testPackCase4.Case.E

operator fun Case.E.plusAssign(konstue: Int) {}
operator fun Case.Inv.invoke(i: Int) {}


// FILE: TestCase4.kt
// TESTCASE NUMBER: 4
package testPackCase4
import libPackage.plusAssign
import libPackage.*
import libPackage.invoke

operator fun Case.E.plusAssign(konstue: Int) {}

class Case() {

    class E(konst plusAssign: Inv? = null) {
        /*operator*/ fun plusAssign(konstue: Int) {}
    }

    class Inv() {
        /*operator*/ fun invoke(konstue: Int) {}
    }

    fun foo(e: E) {
        operator fun E.plusAssign(konstue: Int) {}

        run {
            operator fun E.plusAssign(konstue: Int) {}

            <!DEBUG_INFO_CALL("fqName: testPackCase4.Case.foo.<anonymous>.plusAssign; typeCall: operator extension function")!>e += 1<!>
        }
        <!DEBUG_INFO_CALL("fqName: testPackCase4.Case.foo.plusAssign; typeCall: operator extension function")!>e += 1<!>
    }
}


// FILE: TestCase5.kt
/*
 * TESTCASE NUMBER: 5
 * UNEXPECTED BEHAVIOUR
 * ISSUES: KT-36996
 */
package testPackCase5
import kotlin.reflect.KProperty

class Delegate {
    /*operator*/ fun getValue(thisRef: Any?, property: KProperty<*>): String {
        return ""
    }

    /*operator*/ fun setValue(thisRef: Any?, property: KProperty<*>, konstue: String) {
    }
}

operator fun Delegate.getValue(thisRef: Any?, property: KProperty<*>): String {
    return ""
}
operator fun Delegate.setValue(thisRef: Any?, property: KProperty<*>, konstue: String) {}

fun case() {
    class Test {
        var p: String <!OPERATOR_MODIFIER_REQUIRED, OPERATOR_MODIFIER_REQUIRED!>by<!> Delegate()

        operator fun Delegate.getValue(thisRef: Any?, property: KProperty<*>): String {
            return ""
        }
        operator fun Delegate.setValue(thisRef: Any?, property: KProperty<*>, konstue: String) {
        }
    }
    konst test = Test()
    test.p = "NEW"
    konst x = test.p

}
