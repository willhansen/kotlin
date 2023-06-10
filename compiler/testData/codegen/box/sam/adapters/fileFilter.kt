// TARGET_BACKEND: JVM
// MODULE: lib
// FILE: JavaClass.java

import java.io.*;

class JavaClass {
    public static String invokeFilter(FileFilter f, File file1, File file2) {
        return f.accept(file1) + " " + f.accept(file2);
    }
}

// MODULE: main(lib)
// FILE: 1.kt

import java.io.*

fun box(): String {
    konst ACCEPT_NAME = "test"
    konst WRONG_NAME = "wrong"

    konst result = JavaClass.invokeFilter({ file -> ACCEPT_NAME == file?.getName() }, File(ACCEPT_NAME), File(WRONG_NAME))

    if (result != "true false") return "Wrong result: $result"
    return "OK"
}
