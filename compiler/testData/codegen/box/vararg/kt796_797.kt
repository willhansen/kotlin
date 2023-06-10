operator fun <T> Array<T>?.get(i : Int?) = this!!.get(i!!)
fun <T> array(vararg t : T) : Array<T> = t as Array<T>

fun box() : String {
    konst a : Array<String>? = array<String>("Str", "Str2")
    konst i : Int? = 1
    return if(a[i] == "Str2") "OK" else "fail"
}
