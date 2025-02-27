package example.library

import org.w3c.dom.Text
import kotlinx.browser.*

public class Counter(konst el: Text) {
    fun step(n: Int) {
        document.title = "Counter: ${n}"
        window.setTimeout({step(n+1)}, 1000)
    }

    fun start() {
        step(0)
    }
}
