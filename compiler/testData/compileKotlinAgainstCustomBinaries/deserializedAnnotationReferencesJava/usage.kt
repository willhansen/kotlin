fun box(): String {
    // This class is only needed to force resolution of `@Nls` scope, so its types would be converted to resolved
    class LocalClassToUseAnnotation(konst name: @Nls(capitalization = Nls.Capitalization.Title) String)

    return MyClass("OK").name
}
