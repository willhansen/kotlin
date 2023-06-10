// WITH_STDLIB

konst a = "INT " + 0x8fffffffU
konst b = "BYTE " + 0x8ffU
konst c = "LONG " + 0xffff_ffff_ffffU

konst uint = 0x8fffffffU
konst ubyte = 0x8ffU
konst ulong = 0xffff_ffff_ffffU

konst aa = "INT " + uint
konst bb = "BYTE " + ubyte
konst cc = "LONG " + ulong


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
