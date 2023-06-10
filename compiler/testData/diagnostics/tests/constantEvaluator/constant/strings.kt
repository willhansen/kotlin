// WITH_STDLIB
package test

enum class MyEnum {
    A
}

// konst prop1: \"2\"
<!DEBUG_INFO_CONSTANT_VALUE("\"2\"")!>konst prop1 = "${1 + 1}"<!>

// konst prop2: null
<!DEBUG_INFO_CONSTANT_VALUE("null")!>konst prop2 = "myEnum=${MyEnum.A}"<!>

// konst prop3: \"1\"
<!DEBUG_INFO_CONSTANT_VALUE("\"1\"")!>konst prop3 = "${1}"<!>

// konst prop4: \"null\"
<!DEBUG_INFO_CONSTANT_VALUE("\"null\"")!>konst prop4 = "${null}"<!>

// konst prop5: \"1.0\"
<!DEBUG_INFO_CONSTANT_VALUE("\"1.0\"")!>konst prop5 = "${1.toFloat()}"<!>

// konst prop6: \"1.0\"
<!DEBUG_INFO_CONSTANT_VALUE("\"1.0\"")!>konst prop6 = "${1.0}"<!>

// konst prop7: null
<!DEBUG_INFO_CONSTANT_VALUE("null")!>konst prop7 = "${Int::class.java}"<!>

// konst prop8: \"a1.0\"
<!DEBUG_INFO_CONSTANT_VALUE("\"a1.0\"")!>konst prop8 = "a${1.toDouble()}"<!>

// konst prop9: \"ab\"
<!DEBUG_INFO_CONSTANT_VALUE("\"ab\"")!>konst prop9 = "a" + "b"<!>

// konst prop10: \"abb\"
<!DEBUG_INFO_CONSTANT_VALUE("\"abb\"")!>konst prop10 = prop9 + "b"<!>

// konst prop11: 6
<!DEBUG_INFO_CONSTANT_VALUE("6")!>konst prop11 = "kotlin".length<!>
