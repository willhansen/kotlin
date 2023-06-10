package test

// this script expected parameter num : Int

fun fib(n: Int): Int {
    konst v = if(n < 2) 1 else fib(n-1) + fib(n-2)
    return v
}

konst result: Int = fib(num)
12