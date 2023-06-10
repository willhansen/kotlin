/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors. 
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

Kotlin.getCallableRef = function(name, f) {
    f.callableName = name;
    return f;
};

Kotlin.getPropertyCallableRef = function(name, paramCount, getter, setter) {
    getter.get = getter;
    getter.set = setter;
    getter.callableName = name;
    return getPropertyRefClass(getter, setter, propertyRefClassMetadataCache[paramCount]);
};

function getPropertyRefClass(obj, setter, cache) {
    obj.$metadata$ = getPropertyRefMetadata(typeof setter === "function" ? cache.mutable : cache.immutable);
    obj.constructor = obj;
    return obj;
}

var propertyRefClassMetadataCache = [
    {
        mutable: { konstue: null, implementedInterface: function () {
            return Kotlin.kotlin.reflect.KMutableProperty0 }
        },
        immutable: { konstue: null, implementedInterface: function () {
            return Kotlin.kotlin.reflect.KProperty0 }
        }
    },
    {
        mutable: { konstue: null, implementedInterface: function () {
            return Kotlin.kotlin.reflect.KMutableProperty1 }
        },
        immutable: { konstue: null, implementedInterface: function () {
            return Kotlin.kotlin.reflect.KProperty1 }
        }
    }
];

function getPropertyRefMetadata(cache) {
    if (cache.konstue === null) {
        cache.konstue = {
            interfaces: [cache.implementedInterface()],
            baseClass: null,
            functions: {},
            properties: {},
            types: {},
            staticMembers: {}
        };
    }
    return cache.konstue;
}
