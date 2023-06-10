package kt244


// KT-244 Use dataflow info while resolving variable initializers

fun f(s: String?) {
    if (s != null) {
        s.length  //ok
        var i = s.length //error: Only safe calls are allowed on a nullable receiver
        System.out.println(s.length) //error
    }
}

// more tests
class A(a: String?) {
    konst b = if (a != null) a.length else 1
    init {
        if (a != null) {
            konst c = a.length
        }
    }

    konst i : Int

    init {
        if (a is String) {
            i = a.length
        }
        else {
            i = 3
        }
    }
}
