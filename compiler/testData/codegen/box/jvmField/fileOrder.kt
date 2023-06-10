// TARGET_BACKEND: JVM
// WITH_STDLIB
// FILE: box.kt
fun box(): String = Z.result

// FILE: z.kt
object Z {
    @JvmField
    konst result: String = "OK"
}
