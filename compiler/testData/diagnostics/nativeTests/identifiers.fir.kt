// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE -MISSING_DEPENDENCY_SUPERCLASS

// FIXME: rename identifiers.kt

// FILE: 1.kt
package `check.pkg`

// FILE: 2.kt
package totally.normal.pkg

class <!INVALID_CHARACTERS_NATIVE_ERROR!>`Check.Class`<!>
class NormalClass {
    fun <!INVALID_CHARACTERS_NATIVE_ERROR!>`check$member`<!>() {}
}

object <!INVALID_CHARACTERS_NATIVE_ERROR!>`Check;Object`<!>
object NormalObject

data class Pair(konst first: Int, konst <!INVALID_CHARACTERS_NATIVE_ERROR!>`next,one`<!>: Int)

object Delegate {
    operator fun getValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>): Any? = null
}

fun <!INVALID_CHARACTERS_NATIVE_ERROR!>`check(function`<!>() {
    konst <!INVALID_CHARACTERS_NATIVE_ERROR!>`check)variable`<!> = 1
    konst <!INVALID_CHARACTERS_NATIVE_ERROR!>`check[delegated[variable`<!> by Delegate

    konst normalVariable = 2
    konst normalDelegatedVariable by Delegate

    konst (check, <!INVALID_CHARACTERS_NATIVE_ERROR!>`destructuring]declaration`<!>) = Pair(1, 2)
}

fun normalFunction() {}

konst <!INVALID_CHARACTERS_NATIVE_ERROR!>`check{property`<!> = 1
konst <!INVALID_CHARACTERS_NATIVE_ERROR!>`check}delegated}property`<!> by Delegate
konst normalProperty = 2
konst normalDelegatedProperty by Delegate

fun checkValueParameter(<!INVALID_CHARACTERS_NATIVE_ERROR!>`check/parameter`<!>: Int) {}

fun <<!INVALID_CHARACTERS_NATIVE_ERROR!>`check<type<parameter`<!>, normalTypeParameter> checkTypeParameter() {}

enum class <!INVALID_CHARACTERS_NATIVE_ERROR!>`Check>Enum>Entry`<!> {
    <!INVALID_CHARACTERS_NATIVE_ERROR!>`CHECK:ENUM:ENTRY`<!>;
}

typealias <!INVALID_CHARACTERS_NATIVE_ERROR!>`check\typealias`<!> = Any

fun <!INVALID_CHARACTERS_NATIVE_ERROR!>`check&`<!>() {}

fun <!INVALID_CHARACTERS_NATIVE_ERROR!>`check~`<!>() {}

fun <!INVALID_CHARACTERS_NATIVE_ERROR!>`check*`<!>() {}

fun <!INVALID_CHARACTERS_NATIVE_ERROR!>`check?`<!>() {}

fun <!INVALID_CHARACTERS_NATIVE_ERROR!>`check#`<!>() {}

fun <!INVALID_CHARACTERS_NATIVE_ERROR!>`check|`<!>() {}

fun <!INVALID_CHARACTERS_NATIVE_ERROR!>`check§`<!>() {}

fun <!INVALID_CHARACTERS_NATIVE_ERROR!>`check%`<!>() {}

fun <!INVALID_CHARACTERS_NATIVE_ERROR!>`check@`<!>() {}
