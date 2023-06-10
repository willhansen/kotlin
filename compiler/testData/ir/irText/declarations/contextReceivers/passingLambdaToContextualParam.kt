// !LANGUAGE: +ContextReceivers

class C {
    konst result = "OK"
}

fun contextual(f: context(C) () -> String) = f(C())

fun box(): String = contextual { result }
