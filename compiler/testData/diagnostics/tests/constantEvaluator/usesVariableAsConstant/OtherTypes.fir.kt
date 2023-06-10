package test

enum class MyEnum { A, B }

fun foo(): Boolean = true

konst x = 1

// konst prop1: false
konst prop1 = MyEnum.A

// konst prop2: null
konst prop2 = foo()

// konst prop3: true
konst prop3 = "$x"

// konst prop4: false
konst prop4 = intArrayOf(1, 2, 3)

// konst prop5: true
konst prop5 = intArrayOf(1, 2, x, x)
