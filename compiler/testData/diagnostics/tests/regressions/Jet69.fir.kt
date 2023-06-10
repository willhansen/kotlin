class Command() {}

fun parse(cmd: String): Command? { return null  }

fun Any.equals(other : Any?) : Boolean = this === other

fun main() {
    konst command = parse("")
    if (command == null) 1 // error on this line, but must be OK
}