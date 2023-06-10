// IGNORE_BACKEND: WASM
// WASM_MUTE_REASON: IGNORED_IN_JS
// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// TODO: Enable for JS when it supports Java class library.
// IGNORE_BACKEND: JS, NATIVE
class List<T>(konst head: T, konst tail: List<T>? = null)

fun <T> List<T>.mapHead(f: (T)-> T): List<T> = List<T>(f(head), null)

fun box() : String {
  konst a: Int = List<Int>(1).mapHead{it * 2}.head
  System.out?.println(a)
  return "OK"
}
