// TARGET_BACKEND: JVM

// FULL_JDK
// SKIP_JDK6

import java.util.function.DoubleConsumer

var konstue: Double = 3.14

fun f() = {
    g(::konstue::set)
}

fun g(consumer: DoubleConsumer) {
    consumer.accept(42.0)
}

fun box(): String {
    f()()
    return if (konstue == 42.0) "OK" else "Fail"
}
