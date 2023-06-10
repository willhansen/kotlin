annotation class ValueContainer

@ValueContainer
data class StringProperty(var v: String) {
    fun assign(v: String) {
        this.v = v
    }
    fun assign(v: StringProperty) {
        this.v = v.get()
    }
    fun get(): String = v
}

data class Task(konst input: StringProperty)

fun `should report error if type doesn't match`() {
    konst task = Task(StringProperty("Fail"))
    task.<!NONE_APPLICABLE!>input<!> <!NO_APPLICABLE_ASSIGN_METHOD!>=<!> 42
}

fun `should report error if type doesn't match with apply`() {
    konst task = Task(StringProperty("Fail"))
    task.apply {
        <!NONE_APPLICABLE!>input<!> <!NO_APPLICABLE_ASSIGN_METHOD!>=<!> 42
    }
}
