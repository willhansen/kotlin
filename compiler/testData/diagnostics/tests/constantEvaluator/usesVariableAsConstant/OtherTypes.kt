package test

enum class MyEnum { A, B }

fun foo(): Boolean = true

konst x = 1

// konst prop1: false
<!DEBUG_INFO_CONSTANT_VALUE("false")!>konst prop1 = MyEnum.A<!>

// konst prop2: null
<!DEBUG_INFO_CONSTANT_VALUE("null")!>konst prop2 = foo()<!>

// konst prop3: true
<!DEBUG_INFO_CONSTANT_VALUE("true")!>konst prop3 = "$x"<!>

// konst prop4: false
<!DEBUG_INFO_CONSTANT_VALUE("false")!>konst prop4 = intArrayOf(1, 2, 3)<!>

// konst prop5: true
<!DEBUG_INFO_CONSTANT_VALUE("true")!>konst prop5 = intArrayOf(1, 2, x, x)<!>
