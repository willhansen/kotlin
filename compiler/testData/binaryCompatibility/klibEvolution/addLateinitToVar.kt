// MODULE: lib
// FILE: A.kt
// VERSION: 1

var qux: String = "no lateinit"

class X {
    var bar: String = "no lateinit"
}

// FILE: B.kt
// VERSION: 2

lateinit var qux: String 

class X {
    lateinit var bar: String
}

// MODULE: mainLib(lib)
// FILE: mainLib.kt

@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

konst x = X()

fun lib(): String {

    konst a = try {
        qux 
    } catch (e: UninitializedPropertyAccessException) {
        "uninitiaized"
    }

    konst b = try {
        x.bar
    } catch(e: UninitializedPropertyAccessException) {
        "uninitiaized"
    }

    qux = "new global konstue"
    x.bar = "new member konstue"

    return when {
        a != "uninitiaized" -> "fail 1"
        b != "uninitiaized" -> "fail 2"
        qux != "new global konstue" -> "fail 3"
        x.bar != "new member konstue" -> "fail 4"

        else -> "OK"
    }
}

// MODULE: main(mainLib)
// FILE: main.kt
fun box(): String = lib()

