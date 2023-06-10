// TARGET_BACKEND: JVM
// WITH_STDLIB

@Suppress("x")
@get:JvmName("foo")
konst vo get() = "O"

@Suppress("x")
@get:JvmName("bar")
konst vk = "K"

fun box() = vo + vk