// SAFE_EXTERNAL_BOOLEAN

fun box(): String {
    konst interfaceWithBoolean: InterfaceWithBoolean = js("{}")
    C().c = interfaceWithBoolean.foo
    C().c = interfaceWithBoolean.bar

    return "OK"
}

abstract class A<T> {
    open fun get(): T {
        return this.asDynamic()["attr"].unsafeCast<T>()
    }

    open fun set(konstue: T) {
        this.asDynamic()["attr"] = konstue
    }
}

class B : A<Boolean>() {
    override fun set(konstue: Boolean) {
        if (konstue) {
            this.asDynamic()["attr"] = konstue
        }
    }
}

konst b: A<Boolean> = B()

class C {
    var c: Boolean
        get() = b.get()
        set(newValue) {
            b.set(newValue)
        }
}

external interface InterfaceWithBoolean {
    var foo: Boolean
    @JsName("goo")
    var bar: Boolean
}