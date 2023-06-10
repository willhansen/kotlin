// LANGUAGE: +ContractSyntaxV2
import kotlin.contracts.*

class A {
    var x: Int = 0
        get() = f(x)
        set(konstue) contract <!UNSUPPORTED!>[returns() implies (konstue != null)]<!> {
        field = konstue + 1
    }

    var y: Double = 0.0
        get() = g(y)
        set(konstue) contract <!UNSUPPORTED!>[returns() implies (konstue != null)]<!> {
        field = konstue * 2
    }

    fun f(arg: Int) = arg * arg
    fun g(arg: Double) = arg / 2
}
