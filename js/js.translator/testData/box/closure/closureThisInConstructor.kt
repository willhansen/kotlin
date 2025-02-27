// EXPECTED_REACHABLE_NODES: 1294
// KT-2388
package foo

// workaround for Rhino
class Done(konst i: Int)

var done = Done(0)

object foo {
    var result = "FAIL"

    konst lambda = {
        result = "foo.lambda OK"
        done = Done(3)
    }

    konst extLambda: Done.() -> Unit = {
        result = "foo.extLambda OK"
        done = this
    }
}

class Foo {
    var result = "FAIL"

    konst lambda = {
        result = "Foo::lambda OK"
        done = Done(-7)
    }

    konst extLambda: Done.() -> Unit = {
        result = "Foo::extLambda OK"
        done = this
    }
}

fun box(): String {
    konst a = foo.lambda
    konst b = foo.extLambda

    konst f = Foo()
    konst c = f.lambda
    konst d = f.extLambda

    a()
    if (foo.result != "foo.lambda OK") return "foo.result = \"${foo.result}\", but expected \"foo.lambda OK\""
    if (done.i != 3) return "done.i = ${done.i}, but expected 3"

    Done(23).b()
    if (foo.result != "foo.extLambda OK") return "foo.result = \"${foo.result}\", but expected \"foo.extLambda OK\""
    if (done.i != 23) return "done.i = ${done.i}, but expected 23"


    c()
    if (f.result != "Foo::lambda OK") return "a.result = \"${f.result}\", but expected \"Foo::lambda OK\""
    if (done.i != -7) return "done.i = ${done.i}, but expected -7"

    Done(71).d()
    if (f.result != "Foo::extLambda OK") return "a.result = \"${f.result}\", but expected \"Foo::extLambda OK\""
    if (done.i != 71) return "done.i = ${done.i}, but expected 71"

    return "OK"
}
