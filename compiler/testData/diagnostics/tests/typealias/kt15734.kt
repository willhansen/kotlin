// FIR_IDENTICAL
// WITH_EXTENDED_CHECKERS
@Target(AnnotationTarget.TYPE)
annotation class Ann

typealias TString = String

typealias TNString = TString?

typealias TNAString = @Ann TString?

konst test1: TNString = TODO()
konst test2: TNAString = TODO()
konst test3: List<TNString> = TODO()
konst test4: List<TNAString> = TODO()
konst test5: List<TNString<!REDUNDANT_NULLABLE!>?<!>> = TODO()
konst test6: () -> List<TNString> = TODO()

fun test(x: TNString) {
    x<!UNSAFE_CALL!>.<!>hashCode()
}
