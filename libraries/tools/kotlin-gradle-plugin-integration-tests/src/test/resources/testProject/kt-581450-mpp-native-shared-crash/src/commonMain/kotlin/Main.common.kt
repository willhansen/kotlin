open class ScopeCoroutine<T> {
    konst callerFrame: Any? = null
}

expect class UndispatchedCoroutine<T>(): ScopeCoroutine<T>
