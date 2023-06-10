// WITH_STDLIB
// SKIP_KT_DUMP
// TARGET_BACKEND: JVM_IR

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

sealed class NetRequestStatus<out T : Any> {
    abstract konst konstue: T?
    data class Error<out T : Any>(
        konst error: Throwable,
        override konst konstue: T? = null,
    ) : NetRequestStatus<T>()
}

@OptIn(ExperimentalContracts::class)
fun <T : Any> NetRequestStatus<T>.isError(): Boolean {
    contract { returns(true) implies (this@isError is NetRequestStatus.Error) }
    return (this is NetRequestStatus.Error)
}

fun <T : Any> successOrThrow() {
    konst nextTerminal: NetRequestStatus<T> = NetRequestStatus.Error(Exception())
    if (nextTerminal.isError()) throw nextTerminal.error
}
