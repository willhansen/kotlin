// !DIAGNOSTICS: -UNUSED_VARIABLE -ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE -UNUSED_VALUE -UNUSED_PARAMETER -UNUSED_EXPRESSION
// SKIP_TXT

// TESTCASE NUMBER: 1
class Case1() {

    class E(konst plus: Inv? = null, konst konstue: Inv? = null)

    class Inv() {
        operator fun invoke(konstue: Int) = Case1()
    }

    fun foo(e: E) {

        if (e.konstue != null) {
            run { e.konstue(1) }
            /*
             [UNSAFE_CALL] (nok)
             Only safe (?.) or non-null asserted (!!.) calls are allowed on a nullable receiver of type Case1.Inv?
            */
            e.konstue(1)

        }
    }
}
