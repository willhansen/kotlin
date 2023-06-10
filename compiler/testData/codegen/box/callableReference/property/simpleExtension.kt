konst String.id: String
    get() = this

fun box(): String {
    konst pr = String::id

    if (pr.get("123") != "123") return "Fail konstue: ${pr.get("123")}"

    if (pr.name != "id") return "Fail name: ${pr.name}"

    return pr.get("OK")
}
