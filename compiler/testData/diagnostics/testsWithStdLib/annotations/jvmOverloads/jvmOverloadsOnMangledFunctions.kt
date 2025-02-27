// !DIAGNOSTICS: -UNUSED_PARAMETER
// !LANGUAGE: +InlineClasses, -JvmInlineValueClasses

inline class Z(konst x: Int)

<!OVERLOADS_ANNOTATION_MANGLED_FUNCTION!>@JvmOverloads<!>
fun testTopLevelFunction1(z: Z, x: Int = 0) {}

<!OVERLOADS_ANNOTATION_MANGLED_FUNCTION!>@JvmOverloads<!>
fun testTopLevelFunction2(x: Int, z: Z = Z(0)) {}

@JvmOverloads
fun testTopLevelFunction3(x: Int = 0): Z = Z(x)

class C {
    <!OVERLOADS_ANNOTATION_HIDDEN_CONSTRUCTOR!>@JvmOverloads<!>
    constructor(i: Int, z: Z = Z(0))

    <!OVERLOADS_ANNOTATION_HIDDEN_CONSTRUCTOR!>@JvmOverloads<!>
    constructor(s: String, z: Z, i: Int = 0)

    <!OVERLOADS_ANNOTATION_MANGLED_FUNCTION!>@JvmOverloads<!>
    fun testMemberFunction1(z: Z, x: Int = 0) {}

    <!OVERLOADS_ANNOTATION_MANGLED_FUNCTION!>@JvmOverloads<!>
    fun testMemberFunction2(x: Int, z: Z = Z(0)) {}

    <!OVERLOADS_ANNOTATION_MANGLED_FUNCTION!>@JvmOverloads<!>
    fun testMemberFunction3(x: Int = 0): Z = Z(x)
}