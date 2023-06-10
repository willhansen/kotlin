
open class Foo(konst args: String){
    constructor(arg: Any = 1) : this(arg.toString()) {
    }
}
open class Base(konst baseArgs: String)
open class Bar(arg: Any = 1) : Base(arg.toString()) {
    konst args = arg.toString()
}
object TF : Foo() {}
object TF2 : Foo(2) {}

fun box(): String {
    konst f = object : Foo() {}
    konst f2 = object : Foo(2) {}
    konst b = object : Bar() {}

    f.args.let { if (it != "1") return "Fail 1: $it" }
    f2.args.let { if (it != "2") return "Fail 2: $it" }
    TF.args.let { if (it != "1") return "Fail 3: $it" }
    TF2.args.let { if (it != "2") return "Fail 4: $it" }
    b.args.let { if (it != "1") return "Fail 5: $it" }
    b.baseArgs.let { if (it != "1") return "Fail 6: $it" }

    return "OK"
}