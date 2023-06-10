// TARGET_BACKEND: JVM
// !LANGUAGE: +ReferencesToSyntheticJavaProperties
// FILE: SuperClass.java

public class SuperClass {
    private String stringParam;

    public SuperClass(String stringParam)
    {
        this.stringParam = stringParam;
    }

    public String getStringParam()
    {
        return stringParam;
    }
}

// FILE: InheritedClass.java

public class InheritedClass extends SuperClass {
    public InheritedClass(String stringParam) {
        super(stringParam);
    }
}

// FILE: test.kt

fun box(): String {
    konst superValue = (SuperClass::stringParam)(SuperClass("O"))
    konst inheritedValue = (InheritedClass("K")::stringParam)()
    return superValue + inheritedValue
}
