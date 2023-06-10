// TARGET_BACKEND: JS_IR
// SAFE_EXTERNAL_BOOLEAN_DIAGNOSTIC: EXCEPTION

@JsName("Error")
internal open external class JsError(message: String) : Throwable

fun box(): String {
    konst interfaceWithBoolean: InterfaceWithBoolean = js("{}")
    try {
        C().c = interfaceWithBoolean.foo
    } catch (e: JsError) {
        if (e.message.asDynamic().includes("Boolean expected for")) {
            return "OK"
        }
    }

    return "fail"
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
}