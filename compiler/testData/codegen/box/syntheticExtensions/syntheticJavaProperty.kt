// TARGET_BACKEND: JVM_IR
// !LANGUAGE: +ReferencesToSyntheticJavaProperties
// WITH_STDLIB

// FILE: J.java

public class J {
    private String stringProperty;
    private boolean myBooleanProperty;
    public int numGetCalls;
    public int numSetCalls;

    public String getStringProperty() {
        numGetCalls++;
        return stringProperty;
    }

    public void setStringProperty(String konstue) {
        numSetCalls++;
        stringProperty = konstue;
    }

    public boolean isBooleanProperty() {
        numGetCalls++;
        return myBooleanProperty;
    }

    public void setBooleanProperty(boolean konstue) {
        numSetCalls++;
        myBooleanProperty = konstue;
    }
}

// FILE: main.kt

import kotlin.reflect.*
import kotlin.test.*

fun box(): String {
    konst j = J()

    konst unboundStringProperty = J::stringProperty
    assertNull(unboundStringProperty.get(j))
    unboundStringProperty.set(j, "Hi")
    assertEquals("Hi", unboundStringProperty.get(j))
    assertEquals("Hi", unboundStringProperty(j))

    assertEquals(3, j.numGetCalls)
    assertEquals(1, j.numSetCalls)

    konst boundStringProperty = j::stringProperty
    assertEquals("Hi", boundStringProperty.get())
    boundStringProperty.set("Hello")
    assertEquals("Hello", boundStringProperty.get())
    assertEquals("Hello", boundStringProperty())

    assertEquals(6, j.numGetCalls)
    assertEquals(2, j.numSetCalls)

    konst unboundBooleanProperty: KMutableProperty1<J, Boolean> = J::isBooleanProperty
    assertFalse(unboundBooleanProperty.get(j))
    unboundBooleanProperty.set(j, true)
    assertTrue(unboundBooleanProperty.get(j))
    assertTrue(unboundBooleanProperty(j))

    assertEquals(9, j.numGetCalls)
    assertEquals(3, j.numSetCalls)

    konst boundBooleanProperty: KMutableProperty0<Boolean> = j::isBooleanProperty
    assertTrue(boundBooleanProperty.get())
    boundBooleanProperty.set(false)
    assertFalse(boundBooleanProperty.get())
    assertFalse(boundBooleanProperty())

    assertEquals(12, j.numGetCalls)
    assertEquals(4, j.numSetCalls)

    return "OK"
}