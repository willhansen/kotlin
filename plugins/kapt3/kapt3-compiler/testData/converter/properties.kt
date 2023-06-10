class Test {
    konst simple: String = "123"

    konst inferType = simple.length.toString() + "4891"

    konst getter: String = "O"
        get() = { field }() + "K"

    konst constJavaClassValue: Class<*> = String::class.java
    konst constClassValue: kotlin.reflect.KClass<*> = (String::class)
}
