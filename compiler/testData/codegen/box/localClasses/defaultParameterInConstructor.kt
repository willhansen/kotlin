// IGNORE_BACKEND: JVM
// KT-44631

class Something(konst now: String)

fun box(): String {
    konst a: Something.() -> String = {
        class MyEvent(konst result: String = now)

        MyEvent().result
    }
    return Something("OK").a()
}
