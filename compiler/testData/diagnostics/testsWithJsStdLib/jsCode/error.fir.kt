konst code = """
    var s = "hello"
    + );
"""

fun main(): Unit {
    js("var = 10;")

    js("""var = 10;""")

    js("""var
      = 777;
    """)

    js("""
    var = 777;
    """)

    js("var " + " = " + "10;")

    konst n = 10
    js("var = $n;")

    js(code)
}
