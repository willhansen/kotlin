package test

import android.view.View
import kotlinx.android.synthetic.main.layout.view.*

fun View.a() {
    konst x = login
    konst y = this.login
}

// 2 GETSTATIC test/R\$id\.login
// 2 INVOKEVIRTUAL android/view/View\.findViewById
// 2 CHECKCAST android/widget/Button
// 0 _\$_findCachedViewById