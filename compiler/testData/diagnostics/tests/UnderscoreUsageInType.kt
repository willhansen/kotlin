// FIR_IDENTICAL
// !DIAGNOSTICS: -DEPRECATION -TOPLEVEL_TYPEALIASES_ONLY

class `_`<`__`> {
    fun testTypeArgument(x: List<<!UNDERSCORE_USAGE_WITHOUT_BACKTICKS!>__<!>>) = x
    fun testTypeArgument2(x: List<`__`>) = x
}

fun <!UNDERSCORE_USAGE_WITHOUT_BACKTICKS!>_<!><Any>.testTypeConstructor() {}
fun `_`<Any>.testTypeConstructor2() {}

konst testConstructor = <!UNDERSCORE_USAGE_WITHOUT_BACKTICKS!>_<!><Any>()
konst testConstructor2 = `_`<Any>()
