// !LANGUAGE: -PrivateInFileEffectiveVisibility
private class One {
    konst a1 = arrayOf(
            object { konst fy = "text"}
    )
}

fun box() = if (One().a1[0].fy == "text") "OK" else "fail"