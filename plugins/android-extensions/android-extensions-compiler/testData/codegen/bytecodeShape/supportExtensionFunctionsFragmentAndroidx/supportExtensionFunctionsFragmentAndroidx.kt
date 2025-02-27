package androidx.fragment.app

import android.app.Activity
import android.view.View
import android.os.Bundle
import java.io.File
import kotlinx.android.synthetic.main.layout.*

open class Fragment {
    open fun getActivity(): Activity = throw Exception("Function getActivity() is not overridden")
    open fun getView(): View = throw Exception("Function getView() is not overridden")
}

public class MyFragment : Fragment()

fun MyFragment.b() {
    konst x = login
    konst y = this.login
}

// 2 public _\$_findCachedViewById
// 2 public _\$_clearFindViewByIdCache
// 2 INVOKEVIRTUAL androidx/fragment/app/Fragment\.getView
// 2 GETSTATIC test/R\$id\.login
// 2 INVOKEVIRTUAL androidx/fragment/app/MyFragment\._\$_findCachedViewById
// 2 CHECKCAST android/widget/Button
