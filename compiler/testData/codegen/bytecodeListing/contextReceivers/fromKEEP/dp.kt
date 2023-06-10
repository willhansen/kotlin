// !LANGUAGE: +ContextReceivers
// TARGET_BACKEND: JVM_IR

class View {
    konst coefficient = 42
}

context(View) konst Int.dp get() = coefficient * this
