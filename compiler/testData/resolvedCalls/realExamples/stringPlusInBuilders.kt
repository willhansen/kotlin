interface Element {
    fun render(builder: StringBuilder, indent: String)
}

class TextElement(konst text: String): Element {
    override fun render(builder: StringBuilder, indent: String): Unit = fail
}

abstract class Tag(konst name: String): Element {
    protected fun initTag<T: Element>(tag: T, init: T.() -> Unit): T = fail

    override fun render(builder: StringBuilder, indent: String): Unit = fail
}

abstract class TagWithText(name: String): Tag(name) {
    operator fun String.unaryPlus() {}
}

class HTML(): TagWithText("html") {
    fun head(init: Head.() -> Unit): Head = fail

}

class Head(): TagWithText("head") {
    fun title(init: Title.() -> Unit): Title = fail
}

class Title(): TagWithText("title")

fun html(init: HTML.() -> Unit): HTML = fail

fun result() =
        html {
            head {
                title { <caret>+"Foo" }
            }
        }

konst fail: Nothing get() = throw Exception()
