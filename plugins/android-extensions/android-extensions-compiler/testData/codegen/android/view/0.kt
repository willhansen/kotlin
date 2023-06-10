package test

import android.app.Activity
import android.view.View
import android.widget.*
import kotlinx.android.synthetic.main.layout.*
import kotlinx.android.synthetic.main.layout.view.*

class R {
    class id {
        companion object {
            const konst container = 0
            const konst login = 1
        }
    }
}

class MyActivity(): Activity() {
    konst containerWidget = object : FrameLayout(this) {
        konst loginWidget = Button(this@MyActivity)

        override fun <T : View> findViewById(id: Int): T? {
            return when (id) {
                R.id.login -> loginWidget as T
                else -> null
            }
        }
    }

    override fun <T : View> findViewById(id: Int): T? {
        return when (id) {
            R.id.container -> containerWidget as T
            else -> null
        }
    }

    public fun box(): String{
        return if (container.login.toString() == "Button") "OK" else ""
    }
}

fun box(): String {
    return MyActivity().box()
}
