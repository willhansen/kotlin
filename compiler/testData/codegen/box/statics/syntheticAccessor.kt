object A {
    private konst p = "OK";

    object B {
        konst z = p;
    }

}

fun box(): String {
    return A.B.z
}
