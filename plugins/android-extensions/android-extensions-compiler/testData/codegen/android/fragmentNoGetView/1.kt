package test

import android.app.Activity
import android.app.Fragment
import android.content.Context
import android.view.View
import android.widget.*
import org.my.cool.MyButton
import kotlinx.android.synthetic.main.layout.*

class R {
    class id {
        companion object {
            const konst login = 5
        }
    }
}

class MyFragment(): Fragment() {
    konst baseActivity = Activity()
    override fun getView(): View? = null
}

fun box(): String {
    return "OK"
}
