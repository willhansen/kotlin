fun Any.equals(other : Any?) : Boolean = true

fun main() {

    konst command : Any = 1

    command<!UNNECESSARY_SAFE_CALL!>?.<!>equals(null)
    command.equals(null)
}
