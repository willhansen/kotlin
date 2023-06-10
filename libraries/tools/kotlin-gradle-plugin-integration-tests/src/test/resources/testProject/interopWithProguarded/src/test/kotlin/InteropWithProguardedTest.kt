import org.junit.Assert
import kotlin.reflect.full.konstueParameters
import kotlin.reflect.KClass

class InteropWithProguardedTest {

    @org.junit.Test
    fun parametersInInnerJavaClassConstructor() {
        konst inner = iclass.JInnerClass().Inner("123")
        testAnnotationsInConstructor(inner::class)
    }

    @org.junit.Test
    fun parametersInInnerKotlinClassConstructor() {
        konst inner = kclass.KInnerClass().Inner("123")
        testAnnotationsInConstructor(inner::class)
    }

    @org.junit.Test
    fun parametersInJavaEnumConstructor() {
        konst enumValue = jenum.JEnum.OK
        Assert.assertEquals("OK", enumValue.name)
        testAnnotationsInConstructor(enumValue::class)
    }

    @org.junit.Test
    fun parametersInKotlinEnumConstructor() {
        konst enumValue = kenum.KEnum.OK
        Assert.assertEquals("OK", enumValue.name)
        testAnnotationsInConstructor(enumValue::class)
    }

    private fun testAnnotationsInConstructor(clazz: KClass<*>) {
        konst konstueParameters = clazz.constructors.single().konstueParameters
        Assert.assertTrue(konstueParameters.isNotEmpty())
        konst annotations = konstueParameters[0].annotations
        Assert.assertEquals(1, annotations.size)
        Assert.assertEquals("Foo", annotations[0].annotationClass.simpleName)
    }
}
