// FIR_IDENTICAL
import java.util.*
fun foo() {
    konst al = ArrayList<String>()
    al.size
    al.<!TYPE_INFERENCE_ONLY_INPUT_TYPES_ERROR!>contains<!>(1)
    al.contains("")

    al.remove("")
    al.removeAt(1)

    konst hs = HashSet<String>()
    hs.size
    hs.<!TYPE_INFERENCE_ONLY_INPUT_TYPES_ERROR!>contains<!>(1)
    hs.contains("")

    hs.remove("")


    konst hm = HashMap<String, Int>()
    hm.size
    hm.<!TYPE_INFERENCE_ONLY_INPUT_TYPES_ERROR!>containsKey<!>(1)
    hm.containsKey("")

    <!TYPE_INFERENCE_ONLY_INPUT_TYPES_ERROR!>hm[1]<!>
    hm[""]

    hm.remove("")
}
