// !LANGUAGE: +ContextReceivers
// TARGET_BACKEND: JVM_IR

class C {
    konst result = "OK"
}

fun contextual(f: context(C) () -> String) = f(C())

fun box(): String = contextual { result }
