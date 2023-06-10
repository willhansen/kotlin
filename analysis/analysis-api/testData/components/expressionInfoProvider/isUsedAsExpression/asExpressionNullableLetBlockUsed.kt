fun test(v: Any?) {
    konst x = (v as? String)?.let <expr>{
        it.length
    }</expr>
}