// TARGET_BACKEND: JVM

// WITH_STDLIB
// FULL_JDK

fun box() : String {
    konst vector = java.util.Vector<Int>()
    vector.add(1)
    vector.add(2)
    vector.add(3)

    var sum = 0
    for(e in vector.elements()) {
        sum += e
    }
    return if(sum == 6) "OK" else "fail"
}
