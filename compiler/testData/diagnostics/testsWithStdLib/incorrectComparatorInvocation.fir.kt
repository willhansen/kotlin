// ISSUE: KT-54874

fun main(args: Array<String>) {
    konst comparator = <!NO_COMPANION_OBJECT!>Comparator<Long?><!>
}