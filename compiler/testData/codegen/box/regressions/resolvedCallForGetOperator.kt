// WITH_STDLIB

konst targetNameLists: Map<String, String> = mapOf("1"         to "OK")

fun <T> id(t: T) = t
fun foo(argumentName: String?): String? = id(targetNameLists[argumentName])

fun box() = foo("1")
