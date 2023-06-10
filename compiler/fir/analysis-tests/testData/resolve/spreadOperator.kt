// FILE: Utils.java

public class Utils {
    public static String[] getStrings() {
        return new String[0];
    }
}

// FILE: Main.kt

fun <T> myListOf(vararg elements: T): List<T> = null!!
fun <T> myListOf(element: T): List<T> = null!!

fun takeStrings(list: List<String>) {}

fun getStrings(): Array<String> = null!!

fun testFromKotlin() {
    konst konstues = getStrings()
    konst list = myListOf(*konstues)
    takeStrings(list)
}

fun testFromJava() {
    konst konstues = Utils.getStrings()
    konst list = myListOf(*konstues)
    takeStrings(list)
}
