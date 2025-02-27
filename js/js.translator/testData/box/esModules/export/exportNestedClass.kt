// EXPECTED_REACHABLE_NODES: 1252
// IGNORE_BACKEND: JS
// ES_MODULES
// DONT_TARGET_EXACT_BACKEND: JS
// SKIP_DCE_DRIVEN

// MODULE: export_nested_class
// FILE: lib.kt

abstract class A {
    abstract fun foo(k: String): String
}

@JsExport
class B {
    class Foo : A() {
        override fun foo(k: String): String {
            return "O" + k
        }

        fun bar(k: String): String {
            return foo(k)
        }
    }
}

@JsExport
object MyObject {
    class A {
        fun konstueA() = "OK"
    }
    class B {
        fun konstueB() = "OK"
    }
    class C {
        fun konstueC() = "OK"
    }
}

// FILE: main.mjs
// ENTRY_ES_MODULE
import  { B, MyObject } from "./exportNestedClass-export_nested_class_v5.mjs"

export function box() {
    if (new B.Foo().bar("K") != "OK") return "fail 1";
    const myObject = MyObject.getInstance()
    if (new myObject.A().konstueA() != "OK") return "fail 2";
    if (new myObject.B().konstueB() != "OK") return "fail 3";
    if (new myObject.C().konstueC() != "OK") return "fail 4";

    return "OK"
}