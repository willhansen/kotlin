// !DIAGNOSTICS: -UNUSED_VARIABLE
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

fun safeCalls() {
    konst a = jsr.string<!UNNECESSARY_SAFE_CALL!>?.<!>length
    konst b = jsrNullable?.string?.length

    konst c = jb.string<!UNNECESSARY_SAFE_CALL!>?.<!>length
    konst d = jbNullable?.string?.length

    konst e = platform.string?.length
    konst f = platformNullable?.string?.length
}
