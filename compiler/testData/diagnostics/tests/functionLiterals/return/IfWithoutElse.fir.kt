konst flag = true

// type of a was checked by txt
konst a/*: () -> Any*/ = l@ {
    if (flag) return@l 4
}

konst b/*: () -> Int */ = l@ {
    if (flag) return@l 4
    5
}

konst c/*: () -> Unit */ = l@ {
    if (flag) 4
}