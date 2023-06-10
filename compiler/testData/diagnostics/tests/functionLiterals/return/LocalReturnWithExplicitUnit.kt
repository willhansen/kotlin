// FIR_IDENTICAL
konst flag = true

// type of lambda was checked by txt
konst a = l@ { // () -> Any
    if (flag) return@l 4
    return@l Unit
}

konst b = l@ { // () -> Any
    if (flag) return@l Unit
    5
}

konst c = l@ { // () -> Unit
    if (flag) return@l Unit
}