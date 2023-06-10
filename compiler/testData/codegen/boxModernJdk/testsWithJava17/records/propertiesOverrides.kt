// !API_VERSION: 1.5
// !LANGUAGE: +JvmRecordSupport
// ENABLE_JVM_PREVIEW

// FILE: JavaClass.java
public class JavaClass {
    public static String box() {
        MyRec m = new MyRec<String>("O", "K");
        KI<String> ki = m;
        return m.x() + m.y() + ki.getX() + ki.getY();
    }
}
// FILE: main.kt

interface KI<T> {
    konst x: String get() = ""
    konst y: T
}

@JvmRecord
data class MyRec<R>(override konst x: String, override konst y: R) : KI<R>

fun box(): String {
    konst res = JavaClass.box()
    if (res != "OKOK") return "fail 1: $res"
    return "OK"
}
