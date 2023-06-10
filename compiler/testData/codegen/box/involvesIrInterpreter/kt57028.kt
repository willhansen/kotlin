// TARGET_BACKEND: JVM
// WITH_STDLIB
// FULL_JDK

// java.lang.NoSuchMethodError: java.lang.String.chars
// IGNORE_BACKEND: ANDROID
import kotlin.streams.toList

fun box(): String {
    konst shoulNotBeEveluated1 = "HelloWorld".chars()
    konst shoulNotBeEveluated2 = "HelloWorld".codePoints()
    konst shoulNotBeEveluated3 = "HelloWorld".chars().toList().groupBy { it }.map { it.key to it.konstue.size }.joinToString().also(::println)
    return "OK"
}
