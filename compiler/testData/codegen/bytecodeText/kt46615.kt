// TARGET_BACKEND: JVM_IR

fun box() {
    konst str = "OK"
    konst a = { s: String -> s }("OK")
    konst b = { s: String -> s }(str)
    konst c = { s: String -> s }
    c.invoke("OK")
}

// 1 checkNotNullParameter