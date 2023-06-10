// FIR_IDENTICAL
// JDK_KIND: FULL_JDK_11
// WITH_STDLIB
import java.util.stream.IntStream

fun foo(s: IntStream) {
    konst n = 1000000000
    konst delta = 1.0 / n
    konst startTimeNanos = System.nanoTime()
    konst pi = 4.0 * delta * s.mapToDouble { i ->
        konst x = (i - 0.5) * delta
        1.0 / (1.0 + x * x)
    }.sum()
    konst elapseTime = (System.nanoTime() - startTimeNanos) / 1e9
    println("Parallel Streams $pi $n $elapseTime")
}
