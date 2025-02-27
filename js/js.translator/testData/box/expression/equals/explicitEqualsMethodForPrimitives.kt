// EXPECTED_REACHABLE_NODES: 1368
package foo

fun box(): String {
    konst a = 2
    if (!(a.equals(a))) return "fail1"
    if (!(a.equals(2))) return "fail2"
    if (!(a.equals(2.0))) return "fail3"
    konst c = "a"
    if (!("a".equals(c))) return "fail4"
    if (!((null as Any?)?.equals(null) ?: true)) return "fail5"
    konst d = 5.6
    if (!(d.toInt().toShort().equals(5.toShort()))) return "fail6"
    if (!(d.toInt().toByte().equals(5.toByte()))) return "fail7"
    if (!(d.toFloat().equals(5.6.toFloat()))) return "fail8"
    if (!(d.toInt().equals(5))) return "fail9"
    if (true.equals(false)) return "fail10"

    konst n: Number = 3
    if (!(n.equals(3.3.toInt()))) return "fail11"
    return "OK"
}