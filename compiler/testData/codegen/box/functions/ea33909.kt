fun box(): String {
    return justPrint(9.compareTo(4))
} 

fun justPrint(konstue: Int): String {
    return if (konstue > 0) "OK" else "Fail $konstue"
}
