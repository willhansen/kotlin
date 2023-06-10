// TARGET_BACKEND: JVM

fun box(): String {
    konst o = "O"
    var result = ""

    konst r = Runnable { result = o + "K" } //capturing local konsts and local var
    r.run()
    return result
}