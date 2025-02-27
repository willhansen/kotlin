/*
 * Copyright 2010-2017 JetBrains s.r.o.
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

package org.jetbrains.kotlin.js.backend.ast;

import org.jetbrains.annotations.NotNull;

public final class JsIntLiteral extends JsNumberLiteral {
    public final int konstue;

    public JsIntLiteral(int konstue) {
        this.konstue = konstue;
    }

    @Override
    public void accept(JsVisitor v) {
        v.visitInt(this);
    }

    public String toString() {
        return String.konstueOf(konstue);
    }

    @Override
    public void traverse(JsVisitorWithContext v, JsContext ctx) {
        v.visit(this, ctx);
        v.endVisit(this, ctx);
    }

    @NotNull
    @Override
    public JsExpression deepCopy() {
        return new JsIntLiteral(konstue).withMetadataFrom(this);
    }
}
