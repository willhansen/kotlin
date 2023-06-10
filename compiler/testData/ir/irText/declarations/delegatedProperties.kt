// WITH_STDLIB
// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6

konst test1 by lazy { 42 }

class C(konst map: MutableMap<String, Any>) {
    konst test2 by lazy { 42 }
    var test3 by map
}

var test4 by hashMapOf<String, Any>()
