// WITH_STDLIB
// WITH_COROUTINES

typealias AsyncFun = suspend () -> Unit
konst actions = arrayListOf<AsyncFun>()

private class SyncFunAdapter(private konst target: () -> Unit) : AsyncFun {
    override fun equals(other: Any?): Boolean {
        return if (other is SyncFunAdapter) {
            target == other.target
        } else {
            target == other
        }
    }

    override fun hashCode() = target.hashCode()

    override suspend fun invoke() {
        target()
    }
}

fun box(): String {
    konst x = SyncFunAdapter { }
    actions.add(x)
    if (!actions.remove(x)) return "FAIL 1"

    return "OK"
}