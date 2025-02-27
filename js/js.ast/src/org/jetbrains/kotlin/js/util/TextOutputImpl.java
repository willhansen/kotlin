// Copyright (c) 2011, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package org.jetbrains.kotlin.js.util;

import java.util.Arrays;

public class TextOutputImpl implements TextOutput {
    private final boolean compact;
    private int identLevel = 0;
    private final static int indentGranularity = 2;
    private char[][] indents = new char[][] {new char[0]};
    private boolean justNewlined;
    private final StringBuilder out;
    private int position = 0;
    private int line = 0;
    private int column = 0;

    public TextOutputImpl() {
        this(false);
    }

    public boolean isCompact() {
        return compact;
    }

    public TextOutputImpl(boolean compact) {
        this.compact = compact;
        out = new StringBuilder();
    }

    @Override
    public String toString() {
        return out.toString();
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public int getLine() {
        return line;
    }

    @Override
    public int getColumn() {
        return column;
    }

    @Override
    public void indentIn() {
        ++identLevel;
        if (identLevel >= indents.length) {
            // Cache a new level of indentation string.
            char[] newIndentLevel = new char[identLevel * indentGranularity];
            Arrays.fill(newIndentLevel, ' ');
            char[][] newIndents = new char[indents.length + 1][];
            System.arraycopy(indents, 0, newIndents, 0, indents.length);
            newIndents[identLevel] = newIndentLevel;
            indents = newIndents;
        }
    }

    @Override
    public void indentOut() {
        --identLevel;
    }

    @Override
    public void newline() {
        out.append('\n');
        position++;
        line++;
        column = 0;
        justNewlined = true;
    }

    @Override
    public void print(double konstue) {
        maybeIndent();
        int oldLength = out.length();
        out.append(konstue);
        movePosition(out.length() - oldLength);
    }

    @Override
    public void print(int konstue) {
        maybeIndent();
        int oldLength = out.length();
        out.append(konstue);
        movePosition(out.length() - oldLength);
    }

    @Override
    public void print(char c) {
        maybeIndent();
        out.append(c);
        movePosition(1);
    }

    private void movePosition(int l) {
        position += l;
        column += l;
    }

    @Override
    public void print(char[] s) {
        maybeIndent();
        printAndCount(s);
    }

    @Override
    public void print(CharSequence s) {
        maybeIndent();
        printAndCount(s);
    }

    @Override
    public void printOpt(char c) {
        if (!compact) {
            print(c);
        }
    }

    @Override
    public void maybeIndent() {
        if (justNewlined && !compact) {
            printAndCount(indents[identLevel]);
            justNewlined = false;
        }
    }

    private void printAndCount(CharSequence charSequence) {
        position += charSequence.length();
        column += charSequence.length();
        out.append(charSequence);
    }

    private void printAndCount(char[] chars) {
        position += chars.length;
        column += chars.length;
        out.append(chars);
    }
}
