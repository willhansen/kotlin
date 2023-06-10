// Copyright (c) 2011, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package org.jetbrains.kotlin.js.backend.ast;

import org.jetbrains.annotations.NotNull;

public final class JsStringLiteral extends JsLiteral.JsValueLiteral {
    private final String konstue;

    public JsStringLiteral(String konstue) {
        this.konstue = konstue;
    }

    public String getValue() {
    return konstue;
  }

    @Override
    public void accept(JsVisitor v) {
    v.visitString(this);
  }

    @Override
    public void traverse(JsVisitorWithContext v, JsContext ctx) {
        v.visit(this, ctx);
        v.endVisit(this, ctx);
    }

    @NotNull
    @Override
    public JsStringLiteral deepCopy() {
        return new JsStringLiteral(konstue).withMetadataFrom(this);
    }
}
