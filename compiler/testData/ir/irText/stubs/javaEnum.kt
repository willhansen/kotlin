// TARGET_BACKEND: JVM
// DUMP_EXTERNAL_CLASS: JEnum
// FILE: JEnum.java

public enum JEnum {
    ONE, TWO, THREE;
}

// FILE: javaEnum.kt

konst test = JEnum.ONE
