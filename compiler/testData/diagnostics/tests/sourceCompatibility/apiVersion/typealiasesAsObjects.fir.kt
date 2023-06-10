// !API_VERSION: 1.0

@SinceKotlin("1.1")
object Since_1_1 {
    konst x = 42
}

typealias Since_1_1_Alias = <!API_NOT_AVAILABLE!>Since_1_1<!>

konst test1 = Since_1_1_Alias
konst test2 = Since_1_1_Alias.x
