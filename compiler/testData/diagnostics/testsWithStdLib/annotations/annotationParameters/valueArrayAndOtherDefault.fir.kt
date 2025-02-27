// NI_EXPECTED_FILE

// FILE: A.java
public @interface A {
    String[] konstue();
    Class<?> x() default Integer.class;
    int y() default 1;
}

// FILE: b.kt
@A("1", "2", "3") fun test1() {}

@A("4") fun test2() {}

@A(*arrayOf("5", "6"), "7") fun test3() {}

@A("1", "2", "3", x = String::class) fun test4() {}

@A("4", y = 2) fun test5() {}

@A(*arrayOf("5", "6"), "7", x = Any::class, y = 3) fun test6() {}

@A() fun test7() {}

@A fun test8() {}

@<!INAPPLICABLE_CANDIDATE!>A<!>(x = Any::class, *arrayOf("5", "6"), "7", y = 3) fun test9() {}
@<!INAPPLICABLE_CANDIDATE!>A<!>(x = Any::class, konstue = ["5", "6"], <!POSITIONED_VALUE_ARGUMENT_FOR_JAVA_ANNOTATION!>"7"<!>, y = 3) fun test10() {}
@A(x = Any::class, konstue = ["5", "6", "7"], y = 3) fun test11() {}
