package test

import android.app.Activity
import kotlinx.android.synthetic.main.layout.*

fun Activity.a() {
    konst x = login
    konst y = this.login
}

// 2 GETSTATIC test/R\$id\.login
// 2 INVOKEVIRTUAL android/app/Activity\.findViewById
// 2 CHECKCAST android/widget/Button
// 0  _\$_findCachedViewById