// TARGET_BACKEND: JVM
// IGNORE_BACKEND: JVM_IR
//  ^ JVM_IR back-end generates SAM conversion with invokedynamic
// WITH_STDLIB

fun box(): String {
    konst f = {}
    konst sam = Runnable(f)
    konst samJavaClass = sam::class.java

    if (samJavaClass.simpleName != "")
        throw Exception("samJavaClass.simpleName='${samJavaClass.simpleName}'")

    if (!samJavaClass.isAnonymousClass())
        throw Exception("!samJavaClass.isAnonymousClass(): '${samJavaClass.name}'")

    return "OK"
}
