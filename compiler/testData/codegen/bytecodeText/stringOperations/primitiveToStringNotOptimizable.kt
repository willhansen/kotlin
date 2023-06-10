fun main(a: Boolean, b:Byte, c: Short, d: Int, e: Long, f: Float, g: Double, h: Char) {
    a.toString()
    b.toString()
    c.toString()
    d.toString()
    e.toString()
    f.toString()
    g.toString()
    h.toString()
}

/*Check that all "konstueOf" are String ones and there is no boxing*/
// 1 INVOKESTATIC java/lang/String.konstueOf \(Z\)
// 3 INVOKESTATIC java/lang/String.konstueOf \(I\)
// 1 INVOKESTATIC java/lang/String.konstueOf \(C\)
// 1 INVOKESTATIC java/lang/String.konstueOf \(J\)
// 1 INVOKESTATIC java/lang/String.konstueOf \(F\)
// 1 INVOKESTATIC java/lang/String.konstueOf \(D\)
// 8 konstueOf
