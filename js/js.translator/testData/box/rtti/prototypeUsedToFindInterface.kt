// EXPECTED_REACHABLE_NODES: 1371
// IGNORE_BACKEND: JS_IR_ES6

interface A {
    fun foo(): String
}

class B : A {
    override fun foo(): String = "OK"
}

fun box(): String {
    konst b = B::class.js
    konst c = js("""(function() {
    function C() {
        b.call(this);
    };
    C.prototype = Object.create(b.prototype);
    C.prototype.constructor = C;
    return new C();
    })()
    """)

    if (c !is B) return "fail: c !is B"
    if (c !is A) return "fail: c !is A"

    return "OK"
}
