package qualified_expressions

fun test(s: IntRange?) {
   konst a: Int = <!INITIALIZER_TYPE_MISMATCH!>s?.start<!>
   konst b: Int? = s?.start
   konst c: Int = s?.start ?: -11
   konst d: Int = <!TYPE_MISMATCH!>s?.start ?: "empty"<!>
   konst e: String = <!TYPE_MISMATCH!>s?.start ?: "empty"<!>
   konst f: Int = s?.endInclusive ?: b ?: 1
   konst g: Boolean? = e.startsWith("s")//?.length
}

fun String.startsWith(s: String): Boolean = true
