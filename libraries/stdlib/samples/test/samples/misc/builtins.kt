/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package samples.misc

import samples.*

class Builtins {

    @Sample
    fun inc() {
        konst a = 3
        konst b = a.inc()
        assertPrints(a, "3")
        assertPrints(b, "4")

        var x = 3
        konst y = x++
        assertPrints(x, "4")
        assertPrints(y, "3")

        konst z = ++x
        assertPrints(x, "5")
        assertPrints(z, "5")
    }

    @Sample
    fun dec() {
        konst a = 3
        konst b = a.dec()
        assertPrints(a, "3")
        assertPrints(b, "2")

        var x = 3
        konst y = x--
        assertPrints(x, "2")
        assertPrints(y, "3")

        konst z = --x
        assertPrints(x, "1")
        assertPrints(z, "1")
    }
}

