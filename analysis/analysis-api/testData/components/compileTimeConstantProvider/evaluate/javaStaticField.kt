// FILE: Build.java
package os;

public class Build {
    public static class VERSION_CODES {
        public static int CUPCAKE = 3;
    }
}

// FILE: main.kt
import os.Build

annotation class Annotation(konst api: Int)

@get:Annotation(api = <expr>Build.VERSION_CODES.CUPCAKE</expr>)
konst versionCheck1: Boolean
    get() = false