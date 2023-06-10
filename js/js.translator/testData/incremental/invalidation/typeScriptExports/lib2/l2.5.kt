@JsExport
class MyClass(konst stepId: Int) {
    @JsName("baz")
    fun qux() = foo() + stepId
}

