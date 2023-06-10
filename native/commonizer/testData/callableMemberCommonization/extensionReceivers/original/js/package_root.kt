class Planet(konst name: String, konst diameter: Double)

konst intProperty get() = 42
konst Int.intProperty get() = this
konst Short.intProperty get() = toInt()
konst Long.intProperty get() = toInt()
konst String.intProperty get() = length
konst Planet.intProperty get() = diameter.toInt()

fun intFunction() = 42
fun Int.intFunction() = this
fun Short.intFunction() = toInt()
fun Long.intFunction() = toInt()
fun String.intFunction() = length
fun Planet.intFunction() = diameter.toInt()

konst String.mismatchedProperty1 get() = 42
konst mismatchedProperty2 get() = 42

fun String.mismatchedFunction1() = 42
fun mismatchedFunction2() = 42

konst <T> T.propertyWithTypeParameter1 get() = 42
konst <T> T.propertyWithTypeParameter2 get() = 42
konst <T> T.propertyWithTypeParameter3 get() = 42
konst <T : CharSequence> T.propertyWithTypeParameter4: Int get() = length
konst <T : CharSequence> T.propertyWithTypeParameter5: Int get() = length
konst <T : CharSequence> T.propertyWithTypeParameter6: Int get() = length
konst <T : CharSequence> T.propertyWithTypeParameter7: Int get() = length
konst <T> T.propertyWithTypeParameter8 get() = 42
konst <T> T.propertyWithTypeParameter9 get() = 42

fun <T> T.functionWithTypeParameter1() {}
fun <T> T.functionWithTypeParameter2() {}
fun <T> T.functionWithTypeParameter3() {}
fun <T> T.functionWithTypeParameter4() {}
