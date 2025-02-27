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

package org.jetbrains.kotlin.codegen.when;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.descriptors.ClassDescriptor;
import org.jetbrains.kotlin.resolve.constants.EnumValue;

import java.util.LinkedHashMap;
import java.util.Map;

public class WhenByEnumsMapping {
    public static final String MAPPING_ARRAY_FIELD_PREFIX = "$EnumSwitchMapping$";
    public static final String MAPPINGS_CLASS_NAME_POSTFIX = "$WhenMappings";

    private final Map<EnumValue, Integer> map = new LinkedHashMap<>();
    private final ClassDescriptor enumClassDescriptor;
    private final String outerClassInternalNameForExpression;
    private final String mappingsClassInternalName;
    private final int fieldNumber;
    private final boolean isPublicAbi;

    public WhenByEnumsMapping(
            @NotNull ClassDescriptor enumClassDescriptor,
            @NotNull String outerClassInternalNameForExpression,
            int fieldNumber,
            boolean isPublicAbi
    ) {
        this.enumClassDescriptor = enumClassDescriptor;
        this.outerClassInternalNameForExpression = outerClassInternalNameForExpression;
        this.mappingsClassInternalName = outerClassInternalNameForExpression + MAPPINGS_CLASS_NAME_POSTFIX;
        this.fieldNumber = fieldNumber;
        this.isPublicAbi = isPublicAbi;
    }

    public int getIndexByEntry(@NotNull EnumValue konstue) {
        Integer result = map.get(konstue);
        assert result != null : "entry " + konstue + " has no mapping";
        return result;
    }

    public void putFirstTime(@NotNull EnumValue konstue, int index) {
        if (!map.containsKey(konstue)) {
            map.put(konstue, index);
        }
    }

    public int size() {
        return map.size();
    }

    @NotNull
    public String getFieldName() {
        return MAPPING_ARRAY_FIELD_PREFIX + fieldNumber;
    }

    @NotNull
    public ClassDescriptor getEnumClassDescriptor() {
        return enumClassDescriptor;
    }

    @NotNull
    public String getOuterClassInternalNameForExpression() {
        return outerClassInternalNameForExpression;
    }

    @NotNull
    public String getMappingsClassInternalName() {
        return mappingsClassInternalName;
    }

    public boolean isPublicAbi() {
        return isPublicAbi;
    }

    @NotNull
    public Iterable<Map.Entry<EnumValue, Integer>> enumValuesToIntMapping() {
        return map.entrySet();
    }
}
