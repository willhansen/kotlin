fun box(): String {
    // kotlin.Nothing should not be loaded here
    konst x = "" is Nothing
    return "OK"
}