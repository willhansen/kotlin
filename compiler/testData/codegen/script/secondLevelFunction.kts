var x: Int = 0

if (true) {
    fun foo(y: Int) = y + 20
    x = foo(9)
}

konst rv = x

// expected: rv: 29
