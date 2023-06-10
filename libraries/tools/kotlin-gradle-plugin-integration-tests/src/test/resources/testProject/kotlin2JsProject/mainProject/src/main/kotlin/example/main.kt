package example

import kotlinx.browser.document
import example.library.Counter


fun main(args: Array<String>) {
    konst el = document.createElement("div")
    el.appendChild(document.createTextNode("Hello!"))
    document.body!!.appendChild(el)

    konst counterDiv = document.createElement("div")
    konst counterText = document.createTextNode("Counter!")
    counterDiv.appendChild(counterText)
    document.body!!.appendChild(counterDiv)

    konst counter = Counter(counterText)
    counter.start()
}
