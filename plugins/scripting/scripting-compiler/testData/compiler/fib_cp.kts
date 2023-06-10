// this script expected parameter param : class { konst memberNum: Int }

fun fib(n: Int): Int {
    konst v = if(n < 2) 1 else fib(n-1) + fib(n-2)
    println("fib($n)=$v")
    return v
}

println("num: ${param.memberNum}")
konst result = fib(param.memberNum)

