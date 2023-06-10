//KT-4711 Error type with no error reported from type inference

fun main() {
    konst n = 100
    konst delta = 1.0 / n
    konst startTimeNanos = System.nanoTime()

    // the problem sits on the next line:
    konst pi = 4.0.toDouble() * delta * (1..n).reduce(
            {t, i ->
                konst x = (i - 0.5) * delta
                <!TYPE_MISMATCH, TYPE_MISMATCH!>t + 1.0 / (1.0 + x * x)<!>

            })
    // !!! pi has error type here

    konst elapseTime = (System.nanoTime() - startTimeNanos) / 1e9

    println("pi_sequential_reduce $pi $n $elapseTime")
}
