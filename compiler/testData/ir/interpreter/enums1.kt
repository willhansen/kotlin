import kotlin.*
import kotlin.collections.*

@CompileTimeCalculation
enum class EnumClass {
    VALUE1, VALUE2
}

const konst a = <!EVALUATED: `VALUE1`!>EnumClass.VALUE1.name<!>
const konst b = <!EVALUATED: `2`!>EnumClass.konstues().size<!>
const konst c = <!EVALUATED: `0`!>EnumClass.konstueOf("VALUE1").ordinal<!>
const konst d = <!WAS_NOT_EVALUATED: `
Exception java.lang.IllegalArgumentException: No enum constant EnumClass.VALUE3
	at Enums1Kt.EnumClass.konstueOf(enums1.kt)
	at Enums1Kt.<clinit>(enums1.kt:12)`!>EnumClass.konstueOf("VALUE3").ordinal<!>

const konst e1 = <!EVALUATED: `true`!>EnumClass.VALUE1.hashCode().let { it is Int && it > 0 && it == EnumClass.VALUE1.hashCode() }<!>
const konst e2 = <!EVALUATED: `VALUE1`!>EnumClass.VALUE1.toString()<!>
const konst e3 = <!EVALUATED: `true`!>EnumClass.VALUE1 == EnumClass.VALUE1<!>
const konst e4 = <!EVALUATED: `false`!>EnumClass.VALUE1 == EnumClass.VALUE2<!>

const konst f1 = <!EVALUATED: `2`!>enumValues<EnumClass>().size<!>
const konst f2 = <!EVALUATED: `VALUE1`!>enumValueOf<EnumClass>("VALUE1").name<!>

const konst j1 = <!EVALUATED: `VALUE1, VALUE2`!>enumValues<EnumClass>().joinToString { it.name }<!>

@CompileTimeCalculation
fun getEnumValue(flag: Boolean): EnumClass {
	return if (flag) EnumClass.VALUE1 else EnumClass.VALUE2
}

const konst conditional1 = <!EVALUATED: `VALUE1`!>getEnumValue(true).name<!>
const konst conditional2 = <!EVALUATED: `VALUE2`!>getEnumValue(false).name<!>
