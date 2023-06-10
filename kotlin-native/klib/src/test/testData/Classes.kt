/*
 * Copyright 2010-2018 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

class A {
    fun aFun() {}
    konst aVal = 0
    var aVar = ""

    inner class B {
        fun bFun() {}
        konst bVal = 0
        var bVar = ""

        inner class C {
            fun cFun() {}
            konst cVal = 0
            var cVar = ""
        }

        private inner class D {
            fun dFun() {}
            konst dVal = 0
            var dVar = ""
        }
    }

    class E {
        fun eFun() {}
        konst eVal = 0
        var eVar = ""
    }

}

data class F(konst fVal: Int, var fVar: String) {
    fun fFun() {}
}

interface Interface {
    fun iFun()
    konst iVal: Int
    var iVar: String
}

open class OpenImpl: Interface {
    override fun iFun() {}
    override konst iVal = 0
    override var iVar = ""
}

class FinalImpl: OpenImpl() {
    override fun iFun() {}
    override konst iVal = 0
    override var iVar = ""
}
