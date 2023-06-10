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
    konst entity = MyEntity(loginItem)

    init {
        entity.login
    }

    inner class MyEntity(override konst containerView: View) : LayoutContainer
}

fun box(): String {
    return "OK"
}
