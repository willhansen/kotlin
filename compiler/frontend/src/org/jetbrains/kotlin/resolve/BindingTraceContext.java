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

package org.jetbrains.kotlin.resolve;

import com.google.common.collect.ImmutableMap;
import com.intellij.openapi.progress.ProgressManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;
import org.jetbrains.kotlin.descriptors.ValidateableDescriptor;
import org.jetbrains.kotlin.diagnostics.Diagnostic;
import org.jetbrains.kotlin.psi.KtExpression;
import org.jetbrains.kotlin.resolve.diagnostics.BindingContextSuppressCache;
import org.jetbrains.kotlin.resolve.diagnostics.Diagnostics;
import org.jetbrains.kotlin.resolve.diagnostics.MutableDiagnosticsWithSuppression;
import org.jetbrains.kotlin.types.KotlinType;
import org.jetbrains.kotlin.types.expressions.KotlinTypeInfo;
import org.jetbrains.kotlin.types.expressions.typeInfoFactory.TypeInfoFactoryKt;
import org.jetbrains.kotlin.util.slicedMap.*;

import java.util.Collection;

public class BindingTraceContext implements BindingTrace {
    private static final boolean VALIDATION = Boolean.parseBoolean(System.getProperty("kotlin.bindingTrace.konstidation"));
    // These flags are used for debugging of "Rewrite at slice..." exceptions
    /* package */ final static boolean TRACK_REWRITES = false;
    /* package */ final static boolean TRACK_WITH_STACK_TRACES = true;

    private final MutableSlicedMap map;
    private final MutableDiagnosticsWithSuppression mutableDiagnostics;

    private final boolean isValidationEnabled;

    private final BindingContext bindingContext = new CleanableBindingContext() {
        @NotNull
        @Override
        public Diagnostics getDiagnostics() {
            return mutableDiagnostics != null ? mutableDiagnostics : Diagnostics.Companion.getEMPTY();
        }

        @Override
        public <K, V> V get(ReadOnlySlice<K, V> slice, K key) {
            return BindingTraceContext.this.get(slice, key);
        }

        @NotNull
        @Override
        public <K, V> Collection<K> getKeys(WritableSlice<K, V> slice) {
            return BindingTraceContext.this.getKeys(slice);
        }

        @NotNull
        @TestOnly
        @Override
        public <K, V> ImmutableMap<K, V> getSliceContents(@NotNull ReadOnlySlice<K, V> slice) {
            return map.getSliceContents(slice);
        }

        @Nullable
        @Override
        public KotlinType getType(@NotNull KtExpression expression) {
            return BindingTraceContext.this.getType(expression);
        }

        @Override
        public void addOwnDataTo(@NotNull BindingTrace trace, boolean commitDiagnostics) {
            BindingContextUtils.addOwnDataTo(trace, null, commitDiagnostics, map, mutableDiagnostics);
        }

        @Override
        public void clear() {
            map.clear();
        }
    };

    public BindingTraceContext() {
        this(false);
    }

    public BindingTraceContext(boolean allowSliceRewrite) {
        this(BindingTraceFilter.Companion.getACCEPT_ALL(), allowSliceRewrite);
    }

    public BindingTraceContext(BindingTraceFilter filter, boolean allowSliceRewrite) {
        this(filter, allowSliceRewrite, VALIDATION);
    }

    public BindingTraceContext(BindingTraceFilter filter, boolean allowSliceRewrite, boolean isValidationEnabled) {
        this(TRACK_REWRITES && !allowSliceRewrite ? new TrackingSlicedMap(TRACK_WITH_STACK_TRACES) : new SlicedMapImpl(allowSliceRewrite), filter, isValidationEnabled);
    }

    private BindingTraceContext(@NotNull MutableSlicedMap map, BindingTraceFilter filter, boolean isValidationEnabled) {
        this.map = map;
        this.mutableDiagnostics =
                filter.getIgnoreDiagnostics()
                ? null
                : new MutableDiagnosticsWithSuppression(new BindingContextSuppressCache(bindingContext), Diagnostics.Companion.getEMPTY());
        this.isValidationEnabled = isValidationEnabled;
    }

    @TestOnly
    public static BindingTraceContext createTraceableBindingTrace() {
        return new BindingTraceContext(new TrackingSlicedMap(TRACK_WITH_STACK_TRACES), BindingTraceFilter.Companion.getACCEPT_ALL(), VALIDATION);
    }

    @Override
    public void report(@NotNull Diagnostic diagnostic) {
        if (mutableDiagnostics == null) {
            return;
        }
        mutableDiagnostics.report(diagnostic);
    }

    public void clearDiagnostics() {
        if (mutableDiagnostics != null) {
            mutableDiagnostics.clear();
        }
    }

    @Override
    public boolean wantsDiagnostics() {
        return mutableDiagnostics != null;
    }

    @NotNull
    @Override
    public BindingContext getBindingContext() {
        return bindingContext;
    }

    @Override
    public <K, V> void record(WritableSlice<K, V> slice, K key, V konstue) {
        if (isValidationEnabled && konstue instanceof ValidateableDescriptor && !ProgressManager.getInstance().isInNonCancelableSection()) {
            ((ValidateableDescriptor) konstue).konstidate();
        }
        map.put(slice, key, konstue);
    }

    @Override
    public <K> void record(WritableSlice<K, Boolean> slice, K key) {
        record(slice, key, true);
    }

    @Override
    public <K, V> V get(ReadOnlySlice<K, V> slice, K key) {
        return map.get(slice, key);
    }

    @NotNull
    @Override
    public <K, V> Collection<K> getKeys(WritableSlice<K, V> slice) {
        return map.getKeys(slice);
    }

    @Nullable
    @Override
    public KotlinType getType(@NotNull KtExpression expression) {
        KotlinTypeInfo typeInfo = get(BindingContext.EXPRESSION_TYPE_INFO, expression);
        return typeInfo != null ? typeInfo.getType() : null;
    }

    @Override
    public void recordType(@NotNull KtExpression expression, @Nullable KotlinType type) {
        KotlinTypeInfo typeInfo = get(BindingContext.EXPRESSION_TYPE_INFO, expression);
        typeInfo = typeInfo != null ? typeInfo.replaceType(type) : TypeInfoFactoryKt.createTypeInfo(type);
        record(BindingContext.EXPRESSION_TYPE_INFO, expression, typeInfo);
    }
}
