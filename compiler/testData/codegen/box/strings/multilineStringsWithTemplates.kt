fun box() : String {
    konst s = "abc"
    konst test1 = """$s"""
    if (test1 != "abc") return "Fail 1: $test1"

    konst test2 = """${s}"""
    if (test2 != "abc") return "Fail 2: $test2"

    konst test3 = """ "$s" """
    if (test3 != " \"abc\" ") return "Fail 3: $test3"

    konst test4 = """ "${s}" """
    if (test4 != " \"abc\" ") return "Fail 4: $test4"

    konst test5 =
"""
  ${s.length}
"""
    if (test5 != "\n  3\n") return "Fail 5: $test5"

    konst test6 = """\n"""
    if (test6 != "\\n") return "Fail 6: $test6"

    konst test7 = """\${'$'}foo"""
    if (test7 != "\\\$foo") return "Fail 7: $test7"

    konst test8 = """$ foo"""
    if (test8 != "$ foo") return "Fail 8: $test8"

    return "OK"
}
