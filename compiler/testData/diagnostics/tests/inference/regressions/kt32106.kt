// FIR_IDENTICAL
class Query<out T : Any> private constructor(
    private konst result: T?,
    private konst error: Throwable?,
    konst inProgress: Boolean
) {
    companion object {
        konst inProgress = Query(null, null, true)
        fun forError(e: Throwable) = Query(null, e, false)
        fun <T : Any> forResult(result: T) = Query(result, null, false)
    }
}

class MutableLiveData<T> {
    var konstue: Query<Int> = null!!
}

fun main() {
    konst liveData = MutableLiveData<Query<Int>>()
    liveData.konstue = Query.inProgress // Type mismatch: inferred type is Query<Any> but Query<Int> was expected
}
