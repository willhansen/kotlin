konst p: Int? = 1;
konst z: Int? = 2;

fun box(): String {
    if (!(p!! == z!!)) {
        return "OK"
    }
    return "fail"
}