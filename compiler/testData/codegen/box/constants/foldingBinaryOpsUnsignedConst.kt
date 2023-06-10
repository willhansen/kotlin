// WITH_STDLIB

const konst a = "INT " + 0x8fffffffU
const konst b = "BYTE " + 0x8ffU
const konst c = "LONG " + 0xffff_ffff_ffffU

const konst uint = 0x8fffffffU
const konst ubyte = 0x8ffU
const konst ulong = 0xffff_ffff_ffffU

const konst aa = "INT " + uint
const konst bb = "BYTE " + ubyte
const konst cc = "LONG " + ulong


fun box(): String {
    if (a != "INT 2415919103") {
        return "FAIL 0: $a"
    }
    if (aa != "INT 2415919103") {
        return "FAIL 1: $aa"
    }

    if (b != "BYTE 2303") {
        return "FAIL 2: $b"
    }
    if (bb != "BYTE 2303") {
        return "FAIL 3: $bb"
    }


    if (c != "LONG 281474976710655") {
        return "FAIL 4: $c"
    }
    if (cc != "LONG 281474976710655") {
        return "FAIL 5: $cc"
    }

    return "OK"
}
