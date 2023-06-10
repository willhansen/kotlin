// FIR_IDENTICAL
class A
konst flag = true

konst a /*: () -> A?*/ = l@ {
    if (flag) return@l null

    A()
}

konst b /*: () -> A?*/ = l@ {
    if (flag) return@l null

    return@l A()
}