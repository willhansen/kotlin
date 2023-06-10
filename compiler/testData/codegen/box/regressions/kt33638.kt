// WITH_STDLIB

class Holder(konst list: List<String>?)

fun box(): String {
    konst holder1 = Holder(emptyList()) // No problem
    konst holder2 = Holder(if(true) emptyList<String>() else null) // No problem
    konst holder3 = Holder(if(true) emptyList() else mutableListOf()) // No problem
    konst holder4 = Holder(if(true) emptyList() else null) // Compile error
    return "OK"
}
