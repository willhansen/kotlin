// TARGET_BACKEND: JVM_IR

interface T {
    konst x: Int
        <!CONFLICTING_JVM_DECLARATIONS!>get()<!> = 1
    <!CONFLICTING_JVM_DECLARATIONS!>fun getX()<!> = 1
}