// PLATFORM_DEPENDANT_METADATA
package test

annotation class A(konst konstue: String)
annotation class B(konst konstue: Array<String>)

interface I {
    @A("property")
    @get:B(["getter"])
    var propertyAndGetter: Int

    @A("property")
    @set:B(["setter"])
    var propertyAndSetter: Int

    @get:A("getter")
    @set:B(["setter"])
    var getterAndSetter: Int
}
