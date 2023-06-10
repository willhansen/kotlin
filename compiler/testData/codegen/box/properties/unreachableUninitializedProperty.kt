// WITH_STDLIB
// KT-44496

class C {
    konst todo: String = TODO()

    konst uninitializedVal: String

    var uninitializedVar: String
}

class Foo {
    init {
        TODO()
    }

    konst uninitializedVal: String

    var uninitializedVar: String
}

class Bar {
    konst initializedVal = 43

    init {
        TODO()
    }

    konst uninitializedVal: String

    var uninitializedVar: String
}

fun box(): String {
    try {
        C()
        return "Fail"
    } catch (e: NotImplementedError) {
        //OK
    }

    try {
        Foo()
        return "Fail"
    } catch (e: NotImplementedError) {
        //OK
    }

    try {
        Bar()
        return "Fail"
    } catch (e: NotImplementedError) {
        //OK
    }

    return "OK"
}
