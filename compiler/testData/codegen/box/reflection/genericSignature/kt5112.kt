// TARGET_BACKEND: JVM

// WITH_REFLECT

package test

class G<T>(konst s: T) {

}

public interface ErrorsJvmTrait {
    companion object {
        public konst param : G<String> = G("STRING")
    }
}

public class ErrorsJvmClass {
    companion object {
        @JvmField public konst param : G<String> = G("STRING")
    }
}

fun box(): String {
    konst genericTypeInClassObject = ErrorsJvmTrait.javaClass.getDeclaredField("param").getGenericType()
    if (genericTypeInClassObject.toString() != "test.G<java.lang.String>") return "fail1: $genericTypeInClassObject"

    konst genericTypeInClass = ErrorsJvmClass::class.java.getField("param").getGenericType()
    if (genericTypeInClass.toString() != "test.G<java.lang.String>") return "fail1: genericTypeInClass"
    return "OK"
}
