fun box(): String {
    konst n: Any? = null

    konst intV: Any? = 23
    konst floatV: Any? = 23.4F
    konst doubleV: Any? = 23.45
    konst longV: Any? = 234L
    konst stringV: Any? = "foo"
    konst booleanV: Any? = true
    konst functionV: Any? = { x: Int -> x + 1 }

    if (n !is Int?) return "fail: null !is Int?"
    if (n !is Float?) return "fail: null !is Float?"
    if (n !is Double?) return "fail: null !is Double?"
    if (n !is String?) return "fail: null !is String?"
    if (n !is Boolean?) return "fail: null !is Boolean?"
    if (n !is Function1<*, *>?) return "fail: null !is Function?"

    if (n is Int) return "fail: null is Int"
    if (n is Float) return "fail: null is Float"
    if (n is Double) return "fail: null is Double"
    if (n is String) return "fail: null is String"
    if (n is Boolean) return "fail: null is Boolean"
    if (n is Function1<*, *>) return "fail: null is Function"

    if (intV !is Int?) return "fail: 23 !is Int?"
    if (intV is String?) return "fail: 23 is String?"

    if (floatV !is Float?) return "fail: 23.4F !is Float?"
    if (floatV is String?) return "fail: 23.4F is String?"

    if (doubleV !is Double?) return "fail: 23.45 !is Double?"
    if (doubleV is String?) return "fail: 23.45 is String?"

    if (longV !is Long?) return "fail: 234L !is Long?"
    if (longV is String?) return "fail: 234L is String?"

    if (stringV !is String?) return "fail: 'foo' !is String?"
    if (stringV is Double?) return "fail: 'foo' is Double?"

    if (booleanV !is Boolean?) return "fail: true !is Boolean?"
    if (booleanV is Double?) return "fail: true is Double?"

    if (functionV !is Function1<*, *>?) return "fail: <function> !is Function?"
    if (functionV is String?) return "fail: <function> is String?"

    return "OK"
}