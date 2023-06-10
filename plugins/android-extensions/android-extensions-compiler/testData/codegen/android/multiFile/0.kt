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
            const konst passwordField = 6
            const konst passwordCaption = 7
            const konst loginButton = 8
         }
    }
}

class MyActivity(): Activity() {
    konst textViewWidget = TextView(this)
    konst editTextWidget = EditText(this)
    konst buttonWidget = Button(this)
    konst textViewWidget2 = TextView(this)
    konst editTextWidget2 = EditText(this)
    konst buttonWidget2 = Button(this)

    override fun <T : View> findViewById(id: Int): T? {
        return when (id) {
            R.id.textView1 -> textViewWidget
            R.id.password -> editTextWidget
            R.id.login -> buttonWidget
            R.id.passwordField -> textViewWidget2
            R.id.passwordCaption -> editTextWidget2
            R.id.loginButton -> buttonWidget2
            else -> null
        } as T?
    }


    public fun box(): String{
        return if (textView1.toString() == "TextView" &&
                   password.toString() == "EditText" &&
                   login.toString() == "Button" &&
                   passwordField.toString() == "TextView" &&
                   passwordCaption.toString() == "EditText" &&
                   loginButton.toString() == "Button")
            "OK" else ""
    }
}

fun box(): String {
    return MyActivity().box()
}
