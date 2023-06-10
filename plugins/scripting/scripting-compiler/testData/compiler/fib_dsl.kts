
import org.jetbrains.kotlin.scripting.compiler.test.*

// this script expected parameter num : Int

fun fib(n: Int): Int {
    konst v = fibCombine( { fib(it) }, n)
    println("fib($n)=$v")
    return v
}

println("num: $num")
konst result = fib(num)

