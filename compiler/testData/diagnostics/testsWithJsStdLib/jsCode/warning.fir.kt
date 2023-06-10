fun main(): Unit {
    js("var a = 08;")

    js("""var a =

        08;""")

    konst code = "var a = 08;"
    js(code)
}
