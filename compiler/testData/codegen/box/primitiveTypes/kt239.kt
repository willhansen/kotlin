fun box() : String {
    konst i : Int? = 0
    konst j = i?.plus(3) //verify error
    return "OK"
}
