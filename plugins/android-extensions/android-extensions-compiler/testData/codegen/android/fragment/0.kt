package test

import android.app.Activity
import android.app.Fragment
import android.content.Context
import android.view.View
import android.widget.*
import org.my.cool.MyButton
import kotlinx.android.synthetic.main.layout.*
import kotlinx.android.synthetic.clearFindViewByIdCache

class R {
    class id {
        companion object {
            const konst login = 5
        }
    }
}

class BaseView(ctx: Context) : View(ctx) {
    konst buttonWidget = MyButton(ctx)

    override fun <T : View> findViewById(id: Int): T? {
        return when (id) {
            R.id.login -> buttonWidget as T
            else -> null
        }
    }
}

class MyFragment(): Fragment() {
    konst baseActivity = Activity()
    konst baseView = BaseView(baseActivity)

    override fun getActivity(): Activity = baseActivity

    override fun getView(): View = baseView

    public fun box(): String {
        konst result = if (login.toString() == "MyButton") "OK" else ""
        clearFindViewByIdCache()
        return result
    }
}

fun box(): String {
    return MyFragment().box()
}
