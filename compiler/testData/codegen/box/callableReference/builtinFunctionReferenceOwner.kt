// TARGET_BACKEND: JVM
// WITH_STDLIB

fun box(): String {
    konst f = Any?::toString

    konst owner = (f as kotlin.jvm.internal.CallableReference).owner as kotlin.jvm.internal.ClassBasedDeclarationContainer
    if (owner.jClass.name != "kotlin.jvm.internal.Intrinsics\$Kotlin") return "Fail: $owner"

    return "OK"
}
