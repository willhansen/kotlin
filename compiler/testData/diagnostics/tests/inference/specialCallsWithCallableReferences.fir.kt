// WITH_STDLIB
// SKIP_TXT
// !DIAGNOSTICS: -CAST_NEVER_SUCCEEDS -UNCHECKED_CAST -UNUSED_PARAMETER -UNUSED_VARIABLE -OPT_IN_USAGE_ERROR -UNUSED_EXPRESSION

import kotlin.experimental.ExperimentalTypeInference

fun <K> K.bar3(): K = null as K
fun <K> K.foo3(): K = null as K

fun bar2(): Int = 1
fun foo2(): Float = 1f

fun <K> bar4(): K = null as K
fun <K> foo4(): K = null as K

class Foo6

class Foo7<T>
fun foo7() = null as Foo7<Int>

fun poll0(flag: Boolean) {
    konst inv = <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.reflect.KFunction0<kotlin.Int>")!>if (flag) { ::bar2 } else { ::foo4 }<!>
    inv()
}

fun poll1(flag: Boolean) {
    konst inv = if (flag) { ::bar2 } else { ::foo2 }
    inv()
}

fun poll11(flag: Boolean) {
    konst inv = if (flag) { ::bar2 } else { ::foo2 }
    inv()
}

fun poll12(flag: Boolean) {
    konst inv = if (flag) { ::<!UNRESOLVED_REFERENCE!>bar3<!> } else { ::<!UNRESOLVED_REFERENCE!>foo3<!> }
    <!NO_VALUE_FOR_PARAMETER!>inv()<!>
}

fun poll13(flag: Boolean) {
    konst inv = if (flag) { ::bar2 } else { ::<!UNRESOLVED_REFERENCE!>foo3<!> }
    inv()
}

fun poll14(flag: Boolean) {
    konst inv = <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER, NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER, NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>if (flag) { ::bar4 } else { ::foo4 }<!>
    <!NO_VALUE_FOR_PARAMETER!>inv()<!>
}

fun poll15(flag: Boolean) {
    konst inv = if (flag) { ::<!UNRESOLVED_REFERENCE!>bar5<!> } else { ::<!UNRESOLVED_REFERENCE!>foo5<!> }
    <!NO_VALUE_FOR_PARAMETER!>inv()<!>
}

fun poll16(flag: Boolean) {
    konst inv = if (flag) { ::Foo6 } else { ::Foo6 }
    inv()
}

fun poll17(flag: Boolean) {
    konst inv = <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>if (flag) { foo7() } else { ::Foo7 }<!>
    inv
}

fun poll2(flag: Boolean) {
    konst inv = when (flag) { true -> ::<!UNRESOLVED_REFERENCE!>bar<!> else -> ::<!UNRESOLVED_REFERENCE!>foo<!> }
    <!NO_VALUE_FOR_PARAMETER!>inv()<!>
}

fun poll21(flag: Boolean) {
    konst inv = when (flag) { true -> ::bar2 else -> ::foo2 }
    inv()
}

fun poll22(flag: Boolean) {
    konst inv = when (flag) { true -> ::<!UNRESOLVED_REFERENCE!>bar3<!> else -> ::<!UNRESOLVED_REFERENCE!>foo3<!> }
    <!NO_VALUE_FOR_PARAMETER!>inv()<!>
}

fun poll23(flag: Boolean) {
    konst inv = <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER, NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER, NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>when (flag) { true -> ::bar4 else -> ::foo4 }<!>
    <!NO_VALUE_FOR_PARAMETER!>inv()<!>
}

fun poll24(flag: Boolean) {
    konst inv = when (flag) { true -> ::<!UNRESOLVED_REFERENCE!>bar5<!> else -> ::<!UNRESOLVED_REFERENCE!>foo5<!> }
    inv
}

fun poll25(flag: Boolean) {
    konst inv = when (flag) { true -> ::Foo6 else -> ::Foo6 }
    inv
}

fun poll26(flag: Boolean) {
    konst inv = <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER, NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>when (flag) { true -> ::Foo7 false -> foo7() else -> ::Foo7 }<!>
    inv
}

fun poll3(flag: Boolean) {
    konst inv = when (flag) { true -> ::<!UNRESOLVED_REFERENCE!>bar<!> false -> ::<!UNRESOLVED_REFERENCE!>foo<!> }
    <!NO_VALUE_FOR_PARAMETER!>inv()<!>
}

fun poll31(flag: Boolean) {
    konst inv = when (flag) { true -> ::bar2 false -> ::foo2 }
    inv()
}

fun poll32(flag: Boolean) {
    konst inv = when (flag) { true -> ::<!UNRESOLVED_REFERENCE!>bar3<!> false -> ::<!UNRESOLVED_REFERENCE!>foo3<!> }
    <!NO_VALUE_FOR_PARAMETER!>inv()<!>
}

fun poll33(flag: Boolean) {
    konst inv = <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER, NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER, NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>when (flag) { true -> ::bar4 false -> ::foo4 }<!>
    <!NO_VALUE_FOR_PARAMETER!>inv()<!>
}

fun poll34(flag: Boolean) {
    konst inv = when (flag) { true -> ::<!UNRESOLVED_REFERENCE!>bar5<!> false -> ::<!UNRESOLVED_REFERENCE!>foo5<!> }
    inv
}

fun poll35(flag: Boolean) {
    konst inv = when (flag) { true -> ::Foo6 false -> ::Foo6 }
    inv
}

fun poll36(flag: Boolean) {
    konst inv = <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>when (flag) { true -> ::Foo7 false -> foo7() }<!>
    inv
}

fun poll4() {
    konst inv = try { ::<!UNRESOLVED_REFERENCE!>bar<!> } finally { ::<!UNRESOLVED_REFERENCE!>foo<!> }
    <!NO_VALUE_FOR_PARAMETER!>inv()<!>
}

fun poll41() {
    konst inv = try { ::bar2 } finally { ::foo2 }
    inv()
}

fun poll42() {
    konst inv = try { ::<!UNRESOLVED_REFERENCE!>bar3<!> } finally { ::<!UNRESOLVED_REFERENCE!>foo3<!> }
    <!NO_VALUE_FOR_PARAMETER!>inv()<!>
}

fun poll43() {
    konst inv = <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER, NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>try { ::bar4 } finally { ::foo4 }<!>
    <!NO_VALUE_FOR_PARAMETER!>inv()<!>
}

fun poll44() {
    konst inv = try { ::<!UNRESOLVED_REFERENCE!>bar5<!> } finally { ::<!UNRESOLVED_REFERENCE!>foo5<!> }
    <!NO_VALUE_FOR_PARAMETER!>inv()<!>
}

fun poll45() {
    konst inv = try { ::Foo6 } finally { ::Foo6 }
    inv()
}

fun poll46() {
    konst inv = try { foo7() } finally { ::Foo7 }
    inv
}

fun poll5() {
    konst inv = try { ::<!UNRESOLVED_REFERENCE!>bar<!> } catch (e: Exception) { ::<!UNRESOLVED_REFERENCE!>foo<!> } finally { ::<!UNRESOLVED_REFERENCE!>foo<!> }
    <!NO_VALUE_FOR_PARAMETER!>inv()<!>
}

fun poll51() {
    konst inv = try { ::bar2 } catch (e: Exception) { ::foo2 } finally { ::foo2 }
    inv()
}

fun poll52() {
    konst inv = try { ::<!UNRESOLVED_REFERENCE!>bar3<!> } catch (e: Exception) { ::<!UNRESOLVED_REFERENCE!>foo3<!> } finally { ::<!UNRESOLVED_REFERENCE!>foo3<!> }
    <!NO_VALUE_FOR_PARAMETER!>inv()<!>
}

fun poll53() {
    konst inv = <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER, NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER, NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>try { ::bar4 } catch (e: Exception) { ::foo4 } finally { ::foo4 }<!>
    <!NO_VALUE_FOR_PARAMETER!>inv()<!>
}

fun poll54() {
    konst inv = try { ::<!UNRESOLVED_REFERENCE!>bar5<!> } catch (e: Exception) { ::<!UNRESOLVED_REFERENCE!>foo5<!> } finally { ::<!UNRESOLVED_REFERENCE!>foo5<!> }
    <!NO_VALUE_FOR_PARAMETER!>inv()<!>
}

fun poll55() {
    konst inv = try { ::Foo6 } catch (e: Exception) { ::Foo6 } finally { ::Foo6 }
    inv()
}

fun poll56() {
    konst inv = <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>try { ::Foo7 } catch (e: Exception) { foo7() } finally { foo7() }<!>
    inv
}

fun poll6() {
    konst inv = ::<!UNRESOLVED_REFERENCE!>bar<!>
    inv
}

fun poll61() {
    konst inv = ::bar2
    inv
}

fun poll62() {
    konst inv = ::<!UNRESOLVED_REFERENCE!>bar3<!>
    inv
}

fun poll63() {
    konst inv = ::bar4
    inv
}

fun poll64() {
    konst inv = ::<!UNRESOLVED_REFERENCE!>bar5<!>
    inv
}

fun poll65() {
    konst inv = ::Foo6
    inv
}

fun poll66() {
    konst inv = ::Foo7
    inv
}

fun poll7() {
    konst inv = ::<!UNRESOLVED_REFERENCE!>bar<!><!NOT_NULL_ASSERTION_ON_CALLABLE_REFERENCE!>!!<!>
    <!NO_VALUE_FOR_PARAMETER!>inv()<!>
}

fun poll71() {
    konst inv = ::bar2<!NOT_NULL_ASSERTION_ON_CALLABLE_REFERENCE!>!!<!>
    inv()
}

fun poll72() {
    konst inv = ::<!UNRESOLVED_REFERENCE!>bar3<!><!NOT_NULL_ASSERTION_ON_CALLABLE_REFERENCE!>!!<!>
    <!NO_VALUE_FOR_PARAMETER!>inv()<!>
}

fun poll73() {
    konst inv = <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER, NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>::bar4<!NOT_NULL_ASSERTION_ON_CALLABLE_REFERENCE!>!!<!><!>
    inv
}

fun poll74() {
    konst inv = ::<!UNRESOLVED_REFERENCE!>bar5<!><!NOT_NULL_ASSERTION_ON_CALLABLE_REFERENCE!>!!<!>
    inv
}

fun poll75() {
    konst inv = ::Foo6<!NOT_NULL_ASSERTION_ON_CALLABLE_REFERENCE!>!!<!>
    inv
}

fun poll76() {
    konst inv = <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER, NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>::Foo7<!NOT_NULL_ASSERTION_ON_CALLABLE_REFERENCE!>!!<!><!>
    inv
}

fun poll8() {
    konst inv = ::<!UNRESOLVED_REFERENCE!>bar<!> <!INAPPLICABLE_CANDIDATE!>in<!> <!INAPPLICABLE_CANDIDATE!>setOf<!>(::<!UNRESOLVED_REFERENCE!>foo<!>)
    <!UNRESOLVED_REFERENCE!>inv<!>()
}

fun poll81() {
    konst inv = ::bar2 <!TYPE_INFERENCE_ONLY_INPUT_TYPES_ERROR!>in<!> setOf(::foo2)
    <!UNRESOLVED_REFERENCE!>inv<!>()
}

fun poll82() {
    konst inv = ::<!UNRESOLVED_REFERENCE!>bar3<!> <!INAPPLICABLE_CANDIDATE!>in<!> <!INAPPLICABLE_CANDIDATE!>setOf<!>(::<!UNRESOLVED_REFERENCE!>foo3<!>)
    <!UNRESOLVED_REFERENCE!>inv<!>()
}

fun poll83() {
    konst inv = ::bar4 <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>in<!> <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER, NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>setOf<!>(::foo4)
    inv
}

fun poll84() {
    konst inv = ::<!UNRESOLVED_REFERENCE!>bar5<!> <!INAPPLICABLE_CANDIDATE!>in<!> <!INAPPLICABLE_CANDIDATE!>setOf<!>(::<!UNRESOLVED_REFERENCE!>foo5<!>)
    inv
}

fun poll85() {
    konst inv = ::Foo6 in setOf(::Foo6)
    inv
}

fun poll86() {
    konst inv = ::Foo7 <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>in<!> <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER, NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>setOf<!>(::Foo7)
    inv
}

fun poll87() {
    konst inv = ::Foo7 <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER, TYPE_INFERENCE_ONLY_INPUT_TYPES_ERROR!>in<!> setOf(foo7())
    inv
}

fun poll88() {
    konst inv = foo7() in <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER, NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>setOf<!>(::Foo7)
    inv
}
