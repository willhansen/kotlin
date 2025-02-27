// LANGUAGE: +ContractSyntaxV2
import kotlin.contracts.*

fun printStr(str: String?) contract <!UNSUPPORTED!>[
    returns() implies (str != null)
]<!> {
    require(str != null)
    println(str)
}

fun callExactlyOnce(block: () -> Int) contract <!UNSUPPORTED!>[
    callsInPlace(block, InvocationKind.EXACTLY_ONCE)
]<!> {
    konst num = block()
    println(num)
}

fun calculateNumber(block: () -> Int): Int contract <!UNSUPPORTED!>[
    callsInPlace(block, InvocationKind.EXACTLY_ONCE)
]<!> {
    konst num = block()
    return num
}
