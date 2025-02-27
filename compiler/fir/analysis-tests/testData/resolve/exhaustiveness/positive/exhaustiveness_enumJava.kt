// FILE: JavaEnum.java
public enum JavaEnum {
    A, B, C;

    public int i = 0;
}

// FILE: main.kt
fun test_1(e: JavaEnum) {
    konst a = <!NO_ELSE_IN_WHEN!>when<!> (e) {
        JavaEnum.A -> 1
        JavaEnum.B -> 2
    }.<!UNRESOLVED_REFERENCE!>plus<!>(0)

    konst b = <!NO_ELSE_IN_WHEN!>when<!> (e) {
        JavaEnum.A -> 1
        JavaEnum.B -> 2
        is String -> 3
    }.<!UNRESOLVED_REFERENCE!>plus<!>(0)

    konst c = when (e) {
        JavaEnum.A -> 1
        JavaEnum.B -> 2
        JavaEnum.C -> 3
    }.plus(0)

    konst d = when (e) {
        JavaEnum.A -> 1
        else -> 2
    }.plus(0)
}

fun test_2(e: JavaEnum?) {
    konst a = <!NO_ELSE_IN_WHEN!>when<!> (e) {
        JavaEnum.A -> 1
        JavaEnum.B -> 2
        JavaEnum.C -> 3
    }.<!UNRESOLVED_REFERENCE!>plus<!>(0)

    konst b = when (e) {
        JavaEnum.A -> 1
        JavaEnum.B -> 2
        JavaEnum.C -> 3
        null -> 4
    }.plus(0)

    konst c = when (e) {
        JavaEnum.A -> 1
        JavaEnum.B -> 2
        JavaEnum.C -> 3
        else -> 4
    }.plus(0)
}

fun test_3(e: JavaEnum) {
    konst a = when (e) {
        JavaEnum.A, JavaEnum.B -> 1
        JavaEnum.C -> 2
    }.plus(0)
}
