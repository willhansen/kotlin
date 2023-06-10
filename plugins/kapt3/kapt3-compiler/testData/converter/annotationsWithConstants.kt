// EXPECTED_ERROR: class B is public, should be declared in a file named B.java
// EXPECTED_ERROR: class R is public, should be declared in a file named R.java
// EXPECTED_ERROR: class R is public, should be declared in a file named R.java
// EXPECTED_ERROR: class R2 is public, should be declared in a file named R2.java

// FILE: lib/R.java
package lib;

public class R {
    public static class id {
        public final static int textView = 100;
    }
}

// FILE: app/R.java
package app;

public class R {
    public static class layout {
        public final static int mainActivity = 100;
    }
}

// FILE: app/R2.java
package app;

public class R2 { // For ButterKnife library project support
    public static class layout {
        public final static int mainActivity = 100;
    }
}

// FILE: app/B.java
package app;

public class B {
    public static class id {
        public final static int textView = 200;
    }

    public final static boolean a1 = false;
    public final static byte a2 = 1;
    public final static int a3 = 2;
    public final static short a4 = 3;
    public final static long a5 = 4L;
    public final static char a6 = '5';
    public final static float a7 = 6.0f;
    public final static double a8 = 7.0;
    public final static String a9 = "A";
}

// FILE: lib/OnClick.java
package lib;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OnClick {
    int[] konstue() default {};
}

// FILE: test.kt
package app

import lib.R as LibR
import lib.R.id.textView
import lib.OnClick

annotation class Bind(konst id: Int)

annotation class MultiValue(konst ids: IntArray)
annotation class MultiValueString(konst ids: Array<String>)
annotation class MultiValueByte(konst ids: ByteArray)

@Target(AnnotationTarget.FIELD)
annotation class BindField(konst id: Int)

annotation class Anno(
        konst a1: Boolean,
        konst a2: Byte,
        konst a3: Int,
        konst a4: Short,
        konst a5: Long,
        konst a6: Char,
        konst a7: Float,
        konst a8: Double,
        konst a9: String)

class MyActivity {
    @Bind(LibR.id.textView)
    @BindField(LibR.id.textView)
    konst a = 0

    @Bind(lib.R.id.textView)
    @BindField(lib.R.id.textView)
    konst b = 0

    @Bind(app.R.layout.mainActivity)
    @BindField(app.R.layout.mainActivity)
    konst c = 0

    @Bind(R.layout.mainActivity)
    @BindField(R.layout.mainActivity)
    konst d = 0

    @Bind(R2.layout.mainActivity)
    @BindField(R2.layout.mainActivity)
    @Anno(a1 = B.a1, a2 = B.a2, a3 = B.a3, a4 = B.a4, a5 = B.a5, a6 = B.a6, a7 = B.a7, a8 = B.a8, a9 = B.a9)
    konst e = 0

    @Bind(B.id.textView)
    @BindField(B.id.textView)
    konst f = 0

    @Bind(LibR.id.textView)
    fun foo() {}

    @Bind(lib.R.id.textView)
    fun foo2() {}

    @Bind(app.R.layout.mainActivity)
    fun foo3() {}

    @Bind(R.layout.mainActivity)
    fun foo4() {}

    @Bind(R2.layout.mainActivity)
    @Anno(a1 = B.a1, a2 = B.a2, a3 = B.a3, a4 = B.a4, a5 = B.a5, a6 = B.a6, a7 = B.a7, a8 = B.a8, a9 = B.a9)
    fun foo5() {}

    @Bind(B.id.textView)
    fun plainIntConstant() {}

    @MultiValue(ids = [])
    fun multi0() {}

    @MultiValue(ids = [B.id.textView])
    fun multi1() {}

    @MultiValue(ids = [B.id.textView, B.a3])
    fun multi2() {}

    @MultiValue(ids = intArrayOf(B.id.textView, B.a3))
    fun multi3() {}

    @MultiValueString(ids = arrayOf(B.a9))
    fun multi4() {}

    @MultiValueByte(ids = byteArrayOf(B.a2))
    fun multi5() {}

    @OnClick(B.id.textView)
    fun multiJava1() {}

    @OnClick(B.id.textView, app.R.layout.mainActivity)
    fun multiJava2() {}

    const konst propA = B.id.textView
    konst propB = B.id.textView
    var propC = B.id.textView
    @JvmField
    konst propD = B.id.textView
    @JvmField
    var propE = B.id.textView
    konst propF = JJ.b.length
}

object JJ {
    konst b = c()
    fun c() = "42"
}
