package test

annotation class JustString(konst string: String)

annotation class StringArray(konst stringArray: Array<String>)

@JustString("kotlin")
@StringArray(arrayOf())
class C1

@StringArray(arrayOf("java", ""))
class C2
