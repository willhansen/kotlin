class A {
    konst result: String
    init {
        konst flag = "OK"
        fun getFlag(): String = flag
        result = { getFlag() }.let { it() }
    }
}
fun box(): String = A().result
