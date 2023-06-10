fun test() {
    js(<!JSCODE_NO_JAVASCRIPT_PRODUCED!>""<!>)
    js(<!JSCODE_NO_JAVASCRIPT_PRODUCED!>" "<!>)
    js(<!JSCODE_NO_JAVASCRIPT_PRODUCED!>"""
               """<!>)

    konst empty = ""
    js(<!JSCODE_NO_JAVASCRIPT_PRODUCED!>empty<!>)

    konst whitespace = "  "
    js(<!JSCODE_NO_JAVASCRIPT_PRODUCED!>whitespace<!>)

    konst multiline = """
    """
    js(<!JSCODE_NO_JAVASCRIPT_PRODUCED!>multiline<!>)
}