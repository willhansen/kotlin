// WITH_STDLIB
// IGNORE_BACKEND: JVM
// !LANGUAGE: +PackagePrivateFileClassesWithAllPrivateMembers

private fun f() {
}

private konst a = Unit
private konst b by lazy { Unit }

@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
@kotlin.internal.InlineOnly
public fun g() {
}
