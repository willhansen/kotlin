// EXPECTED_REACHABLE_NODES: 1265
// ES_MODULES
// DONT_TARGET_EXACT_BACKEND: JS
// SKIP_MINIFICATION
// SKIP_DCE_DRIVEN
// SKIP_NODE_JS

// See KT-43783

// MODULE: nestedObjectExport
// FILE: lib.kt
@file:JsExport

class Abc {
    companion object AbcCompanion {
        fun xyz(): String = "Companion object method OK"

        konst prop: String
            get() = "Companion object property OK"
    }
}

class Foo {
    companion object {
        fun xyz(): String = "Companion object method OK"

        konst prop: String
            get() = "Companion object property OK"
    }
}

sealed class MyEnum(konst name: String) {
    object A: MyEnum("A")
    object B: MyEnum("B")
    object C: MyEnum("C")
}

object MyObject {
    object A {
        fun konstueA() = "OK"
    }
    object B {
        fun konstueB() = "OK"
    }
    object C {
        fun konstueC() = "OK"
    }
}

// FILE: main.mjs
// ENTRY_ES_MODULE
import { Foo, Abc, MyEnum, MyObject } from "./exportFileWithNestedObject-nestedObjectExport_v5.mjs"

export function box() {
    if (Abc.AbcCompanion.xyz() != 'Companion object method OK') return 'companion object function failure';
    if (Abc.AbcCompanion.prop != 'Companion object property OK') return 'companion object property failure';

    if (Foo.Companion.xyz() != 'Companion object method OK') return 'companion object function failure';
    if (Foo.Companion.prop != 'Companion object property OK') return 'companion object property failure';

    if (MyEnum.A.name != 'A') return 'MyEnum.A failure';
    if (MyEnum.B.name != 'B') return 'MyEnum.B failure';
    if (MyEnum.C.name != 'C') return 'MyEnum.C failure';

    if (MyObject.getInstance().A.konstueA() != "OK") return 'MyObject.A failure';
    if (MyObject.getInstance().B.konstueB() != "OK") return 'MyObject.B failure';
    if (MyObject.getInstance().C.konstueC() != "OK") return 'MyObject.C failure';

    return 'OK';
}
