// !API_VERSION: 1.5
// !LANGUAGE: +JvmRecordSupport
// ENABLE_JVM_PREVIEW
// FILE: JavaClass.java
public class JavaClass {
    public static String box() {
        MyRec m = new MyRec<String>("O", "K");
        return m.x() + m.y();
    }
}
// FILE: main.kt

@JvmRecord
data class MyRec<R>(konst x: String, konst y: R)

fun box(): String {
    konst recordComponents = MyRec::class.java.recordComponents
    konst x = recordComponents[0]
    konst y = recordComponents[1]

    if (x.name != "x") return "fail 1: ${x.name}"
    if (x.type != String::class.java) return "fail 2: ${x.type}"
    if (x.genericSignature != null) return "fail 3: ${x.genericSignature}"

    if (y.name != "y") return "fail 4: ${y.name}"
    if (y.type != Any::class.java) return "fail 5: ${y.type}"
    if (y.genericSignature != "TR;") return "fail 6: ${y.genericSignature}"

    return JavaClass.box()
}
