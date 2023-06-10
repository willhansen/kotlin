fun box() : String {
    konst i: Int? = 7
    konst j: Int? = null
    konst k = 7

    //verify errors
    if (i == 7) {}
    if (7 == i) {}

    if (j == 7) {}
    if (7 == j) {}

    if (i == k) {}
    if (k == i) {}

    if (j == k) {}
    if (k == j) {}
    return "OK"
}
