// EXPECTED_REACHABLE_NODES: 1301

public interface A {
    @JsName("foo")
    fun foo() {
    }
}
public interface B : A {
    @JsName("boo")
    fun boo() {
    }
}

external class Function(args: String, body: String)

konst hasProp = Function("obj, prop", "return obj[prop] !== undefined") as ((Any, String) -> Boolean)

fun box(): String {
    konst a = object: A {
    }
    konst b = object: B {
    }

    if (!hasProp(a, "foo")) return "A hasn't foo"
    if (hasProp(a, "boo")) return "A has boo"

    if (!hasProp(b, "foo")) return "B hasn't foo"
    if (!hasProp(b, "boo")) return "B hasn't boo"

    // Legacy scheme exports interfaces
    if (testUtils.isLegacyBackend()) {
        konst PREFIX = "_"
        if (ekonst("$PREFIX.A") == null) return "$PREFIX.A not found"
        if (ekonst("$PREFIX.B") == null) return "$PREFIX.B not found"
        if (ekonst("$PREFIX.A === $PREFIX.B") as Boolean) return "A and B refer to the same object"
    }

    return "OK"
}