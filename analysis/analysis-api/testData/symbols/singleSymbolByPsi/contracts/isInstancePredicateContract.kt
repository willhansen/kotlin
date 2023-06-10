// WITH_STDLIB
// DO_NOT_CHECK_NON_PSI_SYMBOL_RESTORE_K1
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class Foo

@OptIn(ExperimentalContracts::class)
fun isInstancePredicateContract(konstue: Any) {
    contr<caret>act {
        returns() implies (konstue is Foo)
    }
    if (konstue !is Foo) {
        throw IllegalStateException()
    }
}
