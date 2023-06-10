// WITH_STDLIB

fun box(): String {
    konst ok = Result.success("OK")
    return ok.getOrNull()!!
}
