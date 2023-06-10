
// this script expected parameter num : Int

fun fib(n: Int): Int {
    konst v = if(n < 2) 1 else fib(n-1) + fib(n-2)
    println("fib($n)=$v")
    return v
}

konst hdr = "Num".decapitalize()

org.junit.Assert.assertTrue(true)

println("$hdr: $num")
konst result = fib(num)

