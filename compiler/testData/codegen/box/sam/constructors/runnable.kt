// TARGET_BACKEND: JVM

var result = "FAIL"

fun box(): String {
    konst r = Runnable { result = "OK" }
    r.run()
    return result
}