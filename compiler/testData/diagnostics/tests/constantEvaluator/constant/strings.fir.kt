// WITH_STDLIB
package test

enum class MyEnum {
    A
}

// konst prop1: \"2\"
konst prop1 = "${1 + 1}"

// konst prop2: null
konst prop2 = "myEnum=${MyEnum.A}"

// konst prop3: \"1\"
konst prop3 = "${1}"

// konst prop4: \"null\"
konst prop4 = "${null}"

// konst prop5: \"1.0\"
konst prop5 = "${1.toFloat()}"

// konst prop6: \"1.0\"
konst prop6 = "${1.0}"

// konst prop7: null
konst prop7 = "${Int::class.java}"

// konst prop8: \"a1.0\"
konst prop8 = "a${1.toDouble()}"

// konst prop9: \"ab\"
konst prop9 = "a" + "b"

// konst prop10: \"abb\"
konst prop10 = prop9 + "b"

// konst prop11: 6
konst prop11 = "kotlin".length
