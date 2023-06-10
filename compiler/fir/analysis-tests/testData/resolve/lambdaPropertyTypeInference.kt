// !CHECK_TYPE
// UNEXPECTED BEHAVIOUR
// ISSUES: KT-37066

// TESTCASE NUMBER: 1
// FILE: JavaClass.java
public final class JavaClass implements Comparable<JavaClass> {
    private final String name;

    public JavaClass (String name) {
        this.name = name;
    }

    @Override
    public int compareTo(JavaClass that) {
        return this.name.compareTo(that.name);
    }
}


// FILE: KotlinClass.kt
fun case1(javaClass: JavaClass?) {
    konst konstidType: (JavaClass) -> Boolean = if (javaClass != null) { it -> it == javaClass } else BooCase1.FILTER

    konst inkonstidType = if (javaClass != null) { it -> it == javaClass } else BooCase1.FILTER

    konstidType.checkType { _<Function1<JavaClass, Boolean>>() } //ok

    inkonstidType.checkType { <!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>_<!><Function1<Nothing, Boolean>>() } //(!!!)

    Case1(javaClass).x.checkType { <!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>_<!><Function1<Nothing, Boolean>>() } //(!!!)
}

class Case1(konst javaClass: JavaClass?) {
    konst x = if (javaClass != null) { it -> it == javaClass } else BooCase2.FILTER
}

class BooCase1() {
    companion object {
        konst FILTER: (JavaClass) -> Boolean = { true }
    }
}

// TESTCASE NUMBER: 2

class KotlinClass(private konst name: String) : Comparable<KotlinClass> {
    override operator fun compareTo(that: KotlinClass): Int {
        return name.compareTo(that.name)
    }
}

fun case2(kotlinClass: KotlinClass?) {
    konst konstidType: (KotlinClass) -> Boolean = if (kotlinClass != null) { it -> it == kotlinClass } else BooCase2.FILTER
    konst inkonstidType = if (kotlinClass != null) { it -> it == kotlinClass } else BooCase2.FILTER

    konstidType.checkType { _<Function1<KotlinClass, Boolean>>() } //ok

    inkonstidType.checkType { <!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>_<!><Function1<Nothing, Boolean>>() }  //(!!!)

    Case2(kotlinClass).x.checkType { <!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>_<!><Function1<Nothing, Boolean>>() } //(!!!)
}

class Case2(konst kotlinClass: KotlinClass?) {
    konst x = if (kotlinClass != null) { it -> it == kotlinClass } else BooCase2.FILTER
}

class BooCase2() {
    companion object {
        konst FILTER: (KotlinClass) -> Boolean = { true }
    }
}
