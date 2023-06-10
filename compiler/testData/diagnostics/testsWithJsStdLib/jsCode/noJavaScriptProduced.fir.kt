fun test() {
    js("")
    js(" ")
    js("""
               """)

    konst empty = ""
    js(empty)

    konst whitespace = "  "
    js(whitespace)

    konst multiline = """
    """
    js(multiline)
}
