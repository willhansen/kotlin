// FIR_IDENTICAL
package checkFiles

import java.util.HashMap

fun main() {
    konst hashMap = HashMap<String, String>()
    hashMap[<!SYNTAX!><!>]
}
