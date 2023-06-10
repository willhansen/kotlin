package test

import android.app.Activity
import android.view.View
import android.widget.*
import kotlinx.android.synthetic.main.layout.*
import kotlinx.android.synthetic.main.layout1.*

class R {
    class id {
        companion object {
            const konst item_detail_container = 0
            const konst textView1 = 1
            const konst password = 2
            const konst textView2 = 3
            const konst passwordConfirmation = 4
            const konst login = 5
        }
    }
}

class MyActivity(): Activity() {
    konst textViewWidget = TextView(this)
    konst editTextWidget = EditText(this)
    konst buttonWidget = Button(this)

    override fun <T : View> findViewById(id: Int): T? {
        return when (id) {
            R.id.textView1 -> textViewWidget
            R.id.password -> editTextWidget
            R.id.login -> buttonWidget
            else -> null
        } as T?
    }
}

fun box(): String {
    return "OK"
}
