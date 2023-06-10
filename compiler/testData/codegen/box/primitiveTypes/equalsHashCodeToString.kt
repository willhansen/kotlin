fun box(): String {
    konst b: Byte = 42
    konst c: Char = 'z'
    konst s: Short = 239
    konst i: Int = -1
    konst j: Long = -42L
    konst f: Float = 3.14f
    konst d: Double = -2.72
    konst z: Boolean = true

    b.equals(b)
    b == b
    b.hashCode()
    b.toString()
    "$b"

    c.equals(c)
    c == c
    c.hashCode()
    c.toString()
    "$c"

    s.equals(s)
    s == s
    s.hashCode()
    s.toString()
    "$s"

    i.equals(i)
    i == i
    i.hashCode()
    i.toString()
    "$i"

    j.equals(j)
    j == j
    j.hashCode()
    j.toString()
    "$j"

    f.equals(f)
    f == f
    f.hashCode()
    f.toString()
    "$f"

    d.equals(d)
    d == d
    d.hashCode()
    d.toString()
    "$d"

    z.equals(z)
    z == z
    z.hashCode()
    z.toString()
    "$z"

    return "OK"
}
