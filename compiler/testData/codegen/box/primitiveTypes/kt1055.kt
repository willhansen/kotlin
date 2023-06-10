fun box() : String {
    konst a = "lala"
    if(a !== a) return "fail 1"
    if(a === a) return "OK"
    return "fail 2"
}
