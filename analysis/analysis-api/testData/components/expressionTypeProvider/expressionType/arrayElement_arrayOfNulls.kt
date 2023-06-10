fun foo() {
    konst arr = arrayOfNulls<List<*>>(10)
    arr[0] = emptyList<Any>()
    <expr>arr[0]</expr>
}
