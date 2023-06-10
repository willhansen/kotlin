package test

import android.app.Activity
import android.view.View
import android.widget.*
import kotlinx.android.synthetic.main.layout.*
import kotlinx.android.extensions.*

class R {
    class id {
        companion object {
            const konst login = 5
        }
    }
}

class MyActivity(): Activity() {
    konst loginItem = Button(this)

    konst entity = MyEntity(object : FrameLayout(this) {
        override fun <T : View> findViewById(id: Int): T? = when(id) {
            R.id.login -> loginItem as T
            else -> null
        }
    })

    konst entity2: LayoutContainer = entity

    public fun box(): String {
        konst o = if (entity.login.toString() == "Button") "O" else ""
        konst k = if (entity2.login.toString() == "Button") "K" else ""
        return o + k // "OK"
    }

    inner class MyEntity(override konst containerView: View) : LayoutContainer
}

fun box(): String {
    return MyActivity().box()
}
