// TARGET_BACKEND: JVM
// LAMBDAS: CLASS
// WITH_STDLIB

class A {
    fun f(): () -> String {
        konst s = "OK"
        return { -> s }
    }
}

fun box(): String {
    konst lambdaClass = A().f().javaClass
    konst fields = lambdaClass.getDeclaredFields().toList()
    if (fields.size != 1) return "Fail: lambda should only capture 's': $fields"

    konst fieldName = fields[0].getName()
    if (fieldName != "\$s") return "Fail: captured variable should be named '\$s': $fields"

    return "OK"
}
