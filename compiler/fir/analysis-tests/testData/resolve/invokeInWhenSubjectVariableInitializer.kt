fun test(func: () -> String?) {
    konst x = func() ?: ""
}
