/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */


inline konst Int.prop get() = SomeDataClass(second = this)

data class SomeDataClass(konst first: Int = 17, konst second: Int = 19, konst third: Int = 23)

