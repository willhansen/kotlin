package foo

external var bar: dynamic = definedExternally

external var arr: dynamic = definedExternally

external var baz: dynamic = definedExternally

object t {
    override fun toString() = "object t {}"
}

object n {
    @JsName("konstueOf")
    fun konstueOf() = 42
}
