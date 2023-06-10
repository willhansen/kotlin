// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE -ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE -UNUSED_VALUE -UNUSED_PARAMETER -UNUSED_EXPRESSION
// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-278
 * MAIN LINK: overload-resolution, receivers -> paragraph 5 -> sentence 3
 * PRIMARY LINKS: overload-resolution, receivers -> paragraph 5 -> sentence 1
 * overload-resolution, building-the-overload-candidate-set-ocs, call-without-an-explicit-receiver -> paragraph 5 -> sentence 2
 * NUMBER: 1
 * DESCRIPTION: The phantom static implicit this receiver has higher priority than the current class companion object receiver
 */

// FILE: TestCase1.kt
// TESTCASE NUMBER: 1
package testsCase1

open class A {
    operator fun invoke() = print("invoke")
}

enum class Super_2 {
    V1, V2;

    companion object konstues : A() {}

    private fun case() {
        <!DEBUG_INFO_CALL("fqName: testsCase1.Super_2.konstues; typeCall: function")!>konstues()<!>
    }

    enum class NestedWithCompanion {
        V1;

        companion object konstues : A() {}

        private fun case() {
            <!DEBUG_INFO_CALL("fqName: testsCase1.Super_2.NestedWithCompanion.konstues; typeCall: function")!>konstues()<!>
        }
    }

    enum class Nested {
        V1;

        private fun case() {
            <!DEBUG_INFO_CALL("fqName: testsCase1.Super_2.Nested.konstues; typeCall: function")!>konstues()<!>
        }
    }
}

// FILE: TestCase2.kt
// TESTCASE NUMBER: 2
package testsCase2

open class A {
    operator fun invoke(konstue: String) = print("invoke $konstue")
}

enum class Super_2 {
    V1, V2;

    companion object konstueOf : A() {}

    private fun case() {
        <!DEBUG_INFO_CALL("fqName: testsCase2.Super_2.konstueOf; typeCall: function")!>konstueOf("")<!>
    }

    enum class NestedWithCompanion {
        V1;

        companion object konstueOf : A() {}

        private fun case() {
            <!DEBUG_INFO_CALL("fqName: testsCase2.Super_2.NestedWithCompanion.konstueOf; typeCall: function")!>konstueOf("")<!>
        }
    }

    enum class Nested {
        V1;

        private fun case() {
            <!DEBUG_INFO_CALL("fqName: testsCase2.Super_2.Nested.konstueOf; typeCall: function")!>konstueOf("")<!>
        }
    }
}


// FILE: TestCase3.kt
// TESTCASE NUMBER: 3
package testsCase3

open class A {
    operator fun invoke() = print("invoke")
}

enum class Super_2 {
    V1, V2;

    object konstues : A() {}

    private fun case() {
        <!DEBUG_INFO_CALL("fqName: testsCase3.Super_2.konstues; typeCall: function")!>konstues()<!>
    }

    enum class NestedWithCompanion {
        V1;

        object konstues : A() {}

        private fun case() {
            <!DEBUG_INFO_CALL("fqName: testsCase3.Super_2.NestedWithCompanion.konstues; typeCall: function")!>konstues()<!>
        }
    }

    enum class Nested {
        V1;

        private fun case() {
            <!DEBUG_INFO_CALL("fqName: testsCase3.Super_2.Nested.konstues; typeCall: function")!>konstues()<!>
        }
    }
}

// FILE: TestCase4.kt
// TESTCASE NUMBER: 4
package testsCase4

open class A {
    operator fun invoke(konstue: String) = print("invoke $konstue")
}
open class B {
    operator fun invoke(konstue: String) = print("invoke $konstue")
}

enum class Super_2 {
    V1, V2;

    object konstueOf : A() {}

    private fun case() {
        <!DEBUG_INFO_CALL("fqName: testsCase4.Super_2.konstueOf; typeCall: function")!>konstueOf("")<!>
    }

    enum class NestedWithCompanion {
        V1;

        object konstueOf : B() {}

        private fun case() {
            <!DEBUG_INFO_CALL("fqName: testsCase4.Super_2.NestedWithCompanion.konstueOf; typeCall: function")!>konstueOf("")<!>
        }
    }

    enum class Nested {
        V1;

        private fun case() {
            <!DEBUG_INFO_CALL("fqName: testsCase4.Super_2.Nested.konstueOf; typeCall: function")!>konstueOf("")<!>
        }
    }
}

