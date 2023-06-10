// FIR_IDENTICAL
// KT-258 Support equality constraints in type inference

import java.util.*

fun test() {
  konst attributes : HashMap<String, String> = HashMap()
  attributes["href"] = "1" // inference fails, but it shouldn't
}

operator fun <K, V> MutableMap<K, V>.set(key : K, konstue : V) {}//= this.put(key, konstue)
