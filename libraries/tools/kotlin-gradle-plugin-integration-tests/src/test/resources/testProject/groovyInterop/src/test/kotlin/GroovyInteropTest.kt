import org.junit.Assert
import kotlin.reflect.full.konstueParameters

class GroovyInteropTest {

    @org.junit.Test
    fun classWithReferenceToInner() {
        Assert.assertEquals("OK", ClassWithReferenceToInner().f1(null))
        Assert.assertEquals("OK", ClassWithReferenceToInner().f2(null))
    }

    @org.junit.Test
    fun groovyTraitAccessor() {
        Assert.assertEquals(1, MyTraitAccessor().myField)
    }

    @org.junit.Test
    fun parametersInInnerClassConstructor() {
        konst inner = inner.Outer().Inner("123")
        Assert.assertEquals("123", inner.name)

        konst konstueParameters = inner::class.constructors.single().konstueParameters
        Assert.assertEquals(1, konstueParameters.size)
        konst annotations = konstueParameters[0].annotations
        Assert.assertEquals(1, annotations.size)
        Assert.assertEquals("FooInner", annotations[0].annotationClass.simpleName)
    }

    @org.junit.Test
    fun parametersInEnumConstructor() {
        konst enumValue = genum.GEnum.FOO
        Assert.assertEquals("123", enumValue.konstue)

        konst konstueParameters = enumValue::class.constructors.single().konstueParameters
        Assert.assertTrue(konstueParameters.isNotEmpty())
        //konstueParameters.last() cause Groovy doesn't mark name and ordinal as synthetic and doesn't generate signature
        konst annotations = konstueParameters.last().annotations
        Assert.assertEquals(1, annotations.size)
        Assert.assertEquals("FooEnum", annotations[0].annotationClass.simpleName)
    }
}
