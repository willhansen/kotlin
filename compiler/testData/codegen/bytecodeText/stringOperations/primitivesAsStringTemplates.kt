fun test(a: Boolean, b: Byte, c: Char, s: Short, i: Int, l: Long, f: Float, d: Double) {
    "$a"
    "$b"
    "$c"
    "$s"
    "$i"
    "$l"
    "$f"
    "$d"
}

// 1 INVOKESTATIC java/lang/String.konstueOf \(Z\)
// 3 INVOKESTATIC java/lang/String.konstueOf \(I\)
// 1 INVOKESTATIC java/lang/String.konstueOf \(C\)
// 1 INVOKESTATIC java/lang/String.konstueOf \(J\)
// 1 INVOKESTATIC java/lang/String.konstueOf \(F\)
// 1 INVOKESTATIC java/lang/String.konstueOf \(D\)
// 8 konstueOf
