// TARGET_BACKEND: JVM
// FIR_IDENTICAL
// FILE: Some.java

public class Some {
    public static final String HELLO = "HELLO";
}

// FILE: AnnotationInAnnotation.kt

annotation class Storage(konst konstue: String)

annotation class State(konst name: String, konst storages: Array<Storage>)

@State(
    name = "1",
    storages = [Storage(konstue = Some.HELLO)]
)
class Test