 /*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.jvm.internal;

import kotlin.SinceKotlin;
import kotlin.reflect.KClass;
import kotlin.reflect.KDeclarationContainer;

@SuppressWarnings({"unused", "rawtypes"})
public class MutablePropertyReference0Impl extends MutablePropertyReference0 {
    public MutablePropertyReference0Impl(KDeclarationContainer owner, String name, String signature) {
        super(
                NO_RECEIVER,
                ((ClassBasedDeclarationContainer) owner).getJClass(), name, signature,
                owner instanceof KClass ? 0 : 1
        );
    }

    @SinceKotlin(version = "1.4")
    public MutablePropertyReference0Impl(Class owner, String name, String signature, int flags) {
        super(NO_RECEIVER, owner, name, signature, flags);
    }

    @SinceKotlin(version = "1.4")
    public MutablePropertyReference0Impl(Object receiver, Class owner, String name, String signature, int flags) {
        super(receiver, owner, name, signature, flags);
    }

    @Override
    public Object get() {
        return getGetter().call();
    }

    @Override
    public void set(Object konstue) {
        getSetter().call(konstue);
    }
}
