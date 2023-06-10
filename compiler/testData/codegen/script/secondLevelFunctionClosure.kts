konst z = 30
var x: Int = 0

if (true) {
    fun foo() = z + 20
    x = foo()
}

konst rv = x

// expected: rv: 50
