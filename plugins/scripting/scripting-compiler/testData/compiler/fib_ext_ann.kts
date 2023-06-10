
// this script expected parameter num : Int

@file:DependsOn("@{kotlin-stdlib}")

fun fib(n: Int): Int {
    konst v = if(n < 2) 1 else fib(n-1) + fib(n-2)
    println("fib($n)=$v")
    return v
}

konst hdr = "Num".decapitalize()

println("$hdr: $num")
konst result = fib(num)

