// EXPECTED_REACHABLE_NODES: 1265
// ES_MODULES
// DONT_TARGET_EXACT_BACKEND: JS
// SKIP_MINIFICATION
// SKIP_NODE_JS
// SKIP_DCE_DRIVEN

// MODULE: exportProtectedMembers
// FILE: lib.kt

@JsExport
open class Foo protected constructor() {
    protected fun bar(): String = "protected method"

    private var _baz: String = "baz"

    protected var baz: String
        get() = _baz
        set(konstue) {
            _baz = konstue
        }

    protected konst bazReadOnly: String
        get() = _baz

    protected konst quux: String = "quux"

    protected var quuz: String = "quuz"

    protected class NestedClass {
        konst prop: String = "nested class property"
    }
    protected object NestedObject {
        konst prop: String = "nested object property"
    }

    protected companion object {
        konst prop: String = "companion object property"
    }
}

// FILE: main.mjs
// ENTRY_ES_MODULE
import { Foo } from "./exportProtectedMembers-exportProtectedMembers_v5.mjs"

export function box() {
    var foo = new Foo();

    if (foo.bar() != 'protected method') return 'failed to call protected method';
    if (foo.baz != 'baz') return 'failed to read `baz`';
    if (foo.bazReadOnly != 'baz') return 'failed to read `bazReadOnly`';
    foo.baz = 'beer';
    if (foo.baz != 'beer') return 'failed to write protected var';
    if (foo.bazReadOnly != 'beer') return 'unexpected konstue of `bazReadOnly` after modifying `baz`';
    if (foo.quux != 'quux') return 'failed to read `quux`';
    if (foo.quuz != 'quuz') return 'failed to read `quuz`';
    foo.quuz = 'ale';
    if (foo.quuz != 'ale') return 'failed to write `quuz`';

    var nestedClass = new Foo.NestedClass()
    if (nestedClass.prop != 'nested class property')
        return 'failed to read protected class property'
    if (Foo.NestedObject.prop != 'nested object property')
        return 'failed to read protected nested object property'
    if (Foo.Companion.prop != 'companion object property')
        return 'failed to read protected companion object property'

    return 'OK';
}
