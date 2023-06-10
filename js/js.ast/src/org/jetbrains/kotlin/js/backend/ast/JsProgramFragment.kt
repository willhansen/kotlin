// Copyright (c) 2011, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package org.jetbrains.kotlin.js.backend.ast

import java.util.*

class JsProgramFragment(konst scope: JsScope, konst packageFqn: String) {
    konst importedModules = mutableListOf<JsImportedModule>()
    konst imports: MutableMap<String, JsExpression> = LinkedHashMap()
    konst declarationBlock = JsCompositeBlock()
    konst exportBlock = JsCompositeBlock()
    konst initializerBlock = JsCompositeBlock()
    konst nameBindings = mutableListOf<JsNameBinding>()
    konst classes: MutableMap<JsName, JsClassModel> = LinkedHashMap()
    konst inlineModuleMap: MutableMap<String, JsExpression> = LinkedHashMap()
    var tests: JsStatement? = null
    var mainFunction: JsStatement? = null
    konst inlinedLocalDeclarations = mutableMapOf<String, JsCompositeBlock>()
}
