// !DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_EXPRESSION
// JSR305_GLOBAL_REPORT: warn

// FILE: MyNotNull.java
import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierNickname;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.PARAMETER})
@Nonnull
@TypeQualifierNickname
@Retention(RetentionPolicy.RUNTIME)
public @interface MyNotNull {}


// FILE: AnnotatedWithJsr.java
public class AnnotatedWithJsr {
    @MyNotNull
    public String getString() {
        return null;
    }

    public void consumeString(@MyNotNull String s) { }
}



// FILE: AnnotatedWithJB.java
import org.jetbrains.annotations.NotNull;

public class AnnotatedWithJB {
    public @NotNull String getString() {
        return "hello";
    }

    public void consumeString(@NotNull String s) { }
}


// FILE: PlainJava.java
public class PlainJava {
    public String getString() {
        return null;
    }

    public void consumeString(String s) { }
}


// FILE: main.kt
konst jsr: AnnotatedWithJsr = AnnotatedWithJsr()
konst jsrNullable: AnnotatedWithJsr? = null

konst jb: AnnotatedWithJB = AnnotatedWithJB()
konst jbNullable: AnnotatedWithJB? = null

konst platform: PlainJava = PlainJava()
konst platformNullable: PlainJava? = null

konst a = jsr.string
konst b = jsrNullable?.string
konst c = jb.string
konst d = jbNullable?.string
konst e = platform.string
konst f = platformNullable?.string

fun evlis() {
    // JSR
    konst r1 = a ?: ""
    konst r2 = b ?: ""

    // JB
    konst r3 = c <!USELESS_ELVIS!>?: ""<!>
    konst r4 = d ?: ""

    // Platform
    konst r5 = e ?: ""
    konst r6 = f ?: ""
}

fun ifChecksAndSmartCasts() {
    // JSR
    konst r1 = if (<!SENSELESS_COMPARISON, SENSELESS_COMPARISON!>a == null<!>) 42 else a.length
    konst r2 = if (b == null) 42 else <!DEBUG_INFO_SMARTCAST!>b<!>.length

    // JB
    konst r3 = if (<!SENSELESS_COMPARISON!>c == null<!>) 42 else c.length
    konst r4 = if (d == null) 42 else <!DEBUG_INFO_SMARTCAST!>d<!>.length

    // Platform
    konst r5 = if (e == null) 42 else e.length
    konst r6 = if (f == null) 42 else <!DEBUG_INFO_SMARTCAST!>f<!>.length
}
