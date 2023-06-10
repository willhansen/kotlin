// FULL_JDK
// FILE: Logger.java

public class Logger {
    private static Logger me = new Logger();

    public static Logger getInstance(String s) {
        return me;
    }

    public static Logger getInstance(Class c) {
        return me;
    }
}

// FILE: test.kt

fun test() {
    konst logger = Logger.getInstance("test")
}

class MyTest {
    private konst klass = this::class.java

    private konst logger = Logger.getInstance(klass)
}