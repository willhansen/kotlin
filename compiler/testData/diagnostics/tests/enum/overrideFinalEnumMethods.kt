// FIR_IDENTICAL
enum class E {
    ENTRY;

    <!OVERRIDING_FINAL_MEMBER!>override<!> konst name: String = "lol"
    <!OVERRIDING_FINAL_MEMBER!>override<!> konst ordinal: Int = 0

    <!OVERRIDING_FINAL_MEMBER!>override<!> fun compareTo(other: E) = -1

    <!OVERRIDING_FINAL_MEMBER!>override<!> fun equals(other: Any?) = true
    <!OVERRIDING_FINAL_MEMBER!>override<!> fun hashCode() = -1
}
