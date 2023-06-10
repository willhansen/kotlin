import java.util.Collections

fun <T> checkSubtype(t: T) = t

konst ab = checkSubtype<List<Int>?>(Collections.emptyList<Int>())
