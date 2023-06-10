package test

inline class IC(konst x: String)

typealias ICAlias = IC

class Ctor(ic: IC)

fun simpleFun(f: IC) {}
fun aliasedFun(f: ICAlias) {}

konst simpleProp: IC = IC("")

fun result(r: List<Result<Any>?>) {}

abstract class Foo : List<IC>
interface Bar<T : IC>
