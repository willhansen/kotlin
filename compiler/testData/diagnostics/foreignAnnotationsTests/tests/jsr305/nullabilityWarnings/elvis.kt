// FIR_IDENTICAL
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

object Elvis {
    fun fromJava() {
        konst a = jsr.string ?: ""
        konst b = jsrNullable?.string ?: ""

        konst c = jb.string <!USELESS_ELVIS!>?: ""<!>
        konst d = jbNullable?.string ?: ""

        konst e = platform.string ?: ""
        konst f = platformNullable?.string ?: ""
    }

    fun toJava(nullableString: String?) {
        konst b = jsr.consumeString(nullableString ?: "")
        konst d = jsrNullable?.consumeString(nullableString ?: "")

        konst f = jb.consumeString(nullableString ?: "")
        konst h = jbNullable?.consumeString(nullableString ?: "")

        konst j = platform.consumeString(nullableString ?: "")
        konst l = platformNullable?.consumeString(nullableString ?: "")
    }
}
