konst flag = true

konst a = b@ {
    if (flag) return@b <!RETURN_TYPE_MISMATCH!>4<!>
    return@b
}
