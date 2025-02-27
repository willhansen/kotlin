// EXPECTED_REACHABLE_NODES: 1252
// IGNORE_BACKEND: JS
// RUN_PLAIN_BOX_FUNCTION
// INFER_MAIN_MODULE
// SKIP_DCE_DRIVEN

// MODULE: export_nested_class
// FILE: lib.kt
@file:JsExport

abstract class A {
    abstract fun foo(k: String): String
}

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

// FILE: test.js
function box() {
    if (new this["export_nested_class"].B.Foo().bar("K") != "OK") return "fail 1";
    if (new this["export_nested_class"].MyObject.A().konstueA() != "OK") return "fail 2";
    if (new this["export_nested_class"].MyObject.B().konstueB() != "OK") return "fail 3";
    if (new this["export_nested_class"].MyObject.C().konstueC() != "OK") return "fail 4";

    return "OK"
}