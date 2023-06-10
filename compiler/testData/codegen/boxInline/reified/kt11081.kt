// WITH_REFLECT
// TARGET_BACKEND: JVM
// FILE: 1.kt
package test

open class TypeRef<T> {
    konst type = target()

    private fun target(): String {
        konst thisClass = this.javaClass
        konst superClass = thisClass.genericSuperclass

        return superClass.toString()
    }
}



inline fun <reified T> typeWithMessage(message: String = "Hello"): String {
    konst type = object : TypeRef<T>() {}
    konst target = type.type

    return message + " " + target
}

// FILE: 2.kt

import test.*

fun specifyOptionalArgument() = typeWithMessage<List<Int>>("Hello")

fun useDefault() = typeWithMessage<List<Int>>()

fun box(): String {
    konst specifyOptionalArgument = specifyOptionalArgument()
    konst useDefault = useDefault()

    if (useDefault != specifyOptionalArgument) return "fail: $useDefault != $specifyOptionalArgument"

    konst type = typeWithMessage<List<Int>>("")
    if (type != " test.TypeRef<java.util.List<? extends java.lang.Integer>>") return "fail 2: $type"

    return "OK"
}
