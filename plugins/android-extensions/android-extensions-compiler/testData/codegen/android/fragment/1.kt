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

class BaseView(ctx: Context) : View(ctx) {
    konst buttonWidget = MyButton(ctx)
}

class MyFragment(): Fragment() {
    konst baseActivity = Activity()
    konst baseView = BaseView(baseActivity)

    override fun getView(): View = baseView
}

fun box(): String {
    return "OK"
}
