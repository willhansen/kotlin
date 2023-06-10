annotation class ValueContainer

abstract class AbstractStringProperty(protected var v: String) {
    fun get(): String {
        return v
    }
}

@ValueContainer
class StringProperty(v: String) : AbstractStringProperty(v) {
    fun assign(v: String) {
        this.v = v
    }

    fun assign(v: StringProperty) {
        this.v = v.get()
    }
}

@ValueContainer
class StringPropertyWithPlus(v: String) : AbstractStringProperty(v) {
    fun assign(v: String) {
        this.v = v
    }

    fun assign(o: StringPropertyWithPlus) {
        this.v = o.get()
    }

    operator fun plus(v: String) =
        StringPropertyWithPlus(this.v + v)
}

@ValueContainer
class StringPropertyWithPlusAssign(v: String) : AbstractStringProperty(v) {
    fun assign(v: String) {
        this.v = v
    }

    fun assign(o: StringPropertyWithPlusAssign) {
        this.v = o.get()
    }

    operator fun plusAssign(v: String) {
        this.v += v
    }
}

@ValueContainer
class StringPropertyWithPlusAndPlusAssign(v: String) : AbstractStringProperty(v) {
    fun assign(v: String) {
        this.v = v
    }

    fun assign(o: StringPropertyWithPlusAndPlusAssign) {
        this.v = o.get()
    }

    operator fun plus(v: String) =
        StringPropertyWithPlusAndPlusAssign(this.v + v)

    operator fun plusAssign(v: String) {
        this.v += v
    }
}

data class Task(
    konst konstInput: StringProperty,
    var varInput: StringProperty,

    konst konstInputWithPlus: StringPropertyWithPlus,
    var varInputWithPlus: StringPropertyWithPlus,

    konst konstInputWithPlusAssign: StringPropertyWithPlusAssign,
    var varInputWithPlusAssign: StringPropertyWithPlusAssign,

    konst konstInputWithPlusAndPlusAssign: StringPropertyWithPlusAndPlusAssign,
    var varInputWithPlusAndPlusAssign: StringPropertyWithPlusAndPlusAssign,
)

fun box(): String {
    konst task = Task(
        StringProperty("O"),
        StringProperty("O"),

        StringPropertyWithPlus("O"),
        StringPropertyWithPlus("O"),

        StringPropertyWithPlusAssign("O"),
        StringPropertyWithPlusAssign("O"),

        StringPropertyWithPlusAndPlusAssign("O"),
        StringPropertyWithPlusAndPlusAssign("O")
    )

    task.varInputWithPlus += "K"
    if (task.varInputWithPlus.get() != "OK") return task.varInputWithPlus.get()

    task.konstInputWithPlusAssign += "K"
    if (task.konstInputWithPlusAssign.get() != "OK") return task.konstInputWithPlusAssign.get()
    task.varInputWithPlusAssign += "K"
    if (task.varInputWithPlusAssign.get() != "OK") return task.varInputWithPlusAssign.get()

    task.konstInputWithPlusAndPlusAssign += "K"
    if (task.konstInputWithPlusAndPlusAssign.get() != "OK") return task.konstInputWithPlusAndPlusAssign.get()

    return "OK"
}
