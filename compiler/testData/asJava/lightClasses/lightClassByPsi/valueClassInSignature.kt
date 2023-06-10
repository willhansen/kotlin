@JvmInline
konstue class Some(konst konstue: String)

class RegularClass {
    var classProp: Some = Some("1")
    var classPropImplicit = Some("1")
    var Some.classPropInExtension: Int
        get() = 1
        set(konstue) {}

    fun classFunInReturn(): Some = Some("1")
    fun classFunInImplicitReturn() = Some("1")
    fun classFunInParameter(s: Some) {}
    fun Some.classFunInExtension() {}
}

interface RegularInterface {
    var interfaceProp: Some
    var Some.interfacePropInExtension: Int

    fun interfaceFunInReturn(): Some
    fun interfaceFunInParameter(s: Some)
    fun Some.interfaceFunInExtension()
}
