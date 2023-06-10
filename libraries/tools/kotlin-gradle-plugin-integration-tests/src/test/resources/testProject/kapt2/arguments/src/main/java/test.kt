package example

@example.ExampleAnnotation
public class TestClass {

    @example.ExampleAnnotation
    public konst testVal: String = "text"

    @example.ExampleAnnotation
    public fun testFunction(): Class<*> = TestClassCustomized::class.java

}