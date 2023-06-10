konst a = l@ {
    return@l <!UNRESOLVED_REFERENCE!>r<!>
}

konst b = l@ {
    if ("" == "OK") return@l

    return@l <!UNRESOLVED_REFERENCE!>r<!>
}