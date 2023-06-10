// TARGET_BACKEND: JVM

// FILE: ComponentScans.java
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ComponentScans {
    ComponentScan[] konstue();
}

// FILE: ComponentScan.java
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(ComponentScans.class)
public @interface ComponentScan {
    String[] a() default {};
    String[] b() default {};
    String[] c() default {};
}

// FILE: main.kt
@ComponentScans(
    konstue = [
        ComponentScan(
            a = ["String" <!EVALUATED("StringA")!>+ "A"<!>],
            c = ["String" <!EVALUATED("StringC")!>+ "C"<!>],
            b = ["String" <!EVALUATED("StringB")!>+ "B"<!>],
        )
    ]
)
class JavaTest

annotation class KtComponentScans(
    konst konstue: Array<KtComponentScan> = [],
)

annotation class KtComponentScan(
    konst a: Array<String> = [],
    konst b: Array<String> = [],
    konst c: Array<String> = [],
)

@ComponentScans(
    konstue = [
        ComponentScan(
            a = ["String" <!EVALUATED("StringA")!>+ "A"<!>],
            c = ["String" <!EVALUATED("StringC")!>+ "C"<!>],
            b = ["String" <!EVALUATED("StringB")!>+ "B"<!>],
        )
    ]
)
class KtTest

fun box(): String {
    return "OK"
}
