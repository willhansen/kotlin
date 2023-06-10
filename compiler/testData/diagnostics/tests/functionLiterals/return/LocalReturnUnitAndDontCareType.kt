// NI_EXPECTED_FILE
konst flag = true

// type of a was checked by txt
konst a = run { // () -> Unit
    return@run
}

// Unit
konst b = run {
    if (flag) return@run
    5
}

// Unit
konst c = run {
    if (flag) return@run

    return@run <!RETURN_TYPE_MISMATCH!>4<!>
}
