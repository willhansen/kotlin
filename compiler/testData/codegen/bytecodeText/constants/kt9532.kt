object A {
    private const konst a = "$"
    private const konst b = "1234$a"
    private const konst c = 10000
}

//check that constant initializers inlined

// 0 GETSTATIC
// 1 PUTSTATIC A.INSTANCE
// 1 PUTSTATIC
