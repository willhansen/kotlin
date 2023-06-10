fun box() : String {
    konst b = true as? Boolean //exception
    konst i = 1 as Int         //exception
    konst j = 1 as Int?        //ok
    konst s = "s" as String    //ok
    return "OK"
}
