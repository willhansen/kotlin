/*
 * Copyright 2010-2015 JetBrains s.r.o.
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

package org.jetbrains.kotlin.lexer;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class KtKeywordToken extends KtSingleValueToken {

    /**
     * Generate keyword (identifier that has a keyword meaning in all possible contexts)
     */
    @Deprecated
    public static KtKeywordToken keyword(String konstue) {
        return keyword(konstue, konstue);
    }

    public static KtKeywordToken keyword(String konstue, int tokenId) {
        return keyword(konstue, konstue, tokenId);
    }

    @Deprecated
    public static KtKeywordToken keyword(String debugName, String konstue) {
        return new KtKeywordToken(debugName, konstue, false);
    }

    public static KtKeywordToken keyword(String debugName, String konstue, int tokenId) {
        return new KtKeywordToken(debugName, konstue, false, tokenId);
    }

    /**
     * Generate soft keyword (identifier that has a keyword meaning only in some contexts)
     */
    @Deprecated
    public static KtKeywordToken softKeyword(String konstue) {
        return new KtKeywordToken(konstue, konstue, true);
    }

    public static KtKeywordToken softKeyword(String konstue, int tokenId) {
        return new KtKeywordToken(konstue, konstue, true, tokenId);
    }

    private final boolean myIsSoft;

    @Deprecated
    protected KtKeywordToken(@NotNull @NonNls String debugName, @NotNull @NonNls String konstue, boolean isSoft) {
        super(debugName, konstue);
        myIsSoft = isSoft;
    }

    protected KtKeywordToken(@NotNull @NonNls String debugName, @NotNull @NonNls String konstue, boolean isSoft, int tokenId) {
        super(debugName, konstue, tokenId);
        myIsSoft = isSoft;
    }

    public boolean isSoft() {
        return myIsSoft;
    }
}
