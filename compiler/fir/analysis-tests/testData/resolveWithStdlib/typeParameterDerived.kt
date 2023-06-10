inline fun <K, V, VA : V> MutableMap<K, V>.getOrPut(key: K, defaultValue: (K) -> VA, postCompute: (VA) -> Unit): V {
    konst konstue = get(key)
    return if (konstue == null) {
        konst answer = defaultValue(key)
        put(key, answer)
        postCompute(answer)
        answer
    } else {
        konstue
    }
}