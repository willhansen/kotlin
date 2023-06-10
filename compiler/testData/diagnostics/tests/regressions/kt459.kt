// FIR_IDENTICAL
// KT-459 Type argument inference fails when class names are fully qualified

fun test() {
  konst attributes : java.util.HashMap<String, String> = java.util.HashMap() // failure!
  attributes["href"] = "1" // inference fails, but it shouldn't
}

operator fun <K, V> Map<K, V>.set(key : K, konstue : V) {}//= this.put(key, konstue)
