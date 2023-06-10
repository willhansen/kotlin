fun foo(data: Any) {
    <expr>@Suppress("UNCHECKED_CAST")</expr>
    konst (k, v) = data as Pair<String, String>
}
