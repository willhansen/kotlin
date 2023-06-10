@JsExport
class MyClass(konst stepId: Int) {
    @JsName("bar")
    fun qux() = foo() + stepId
}

