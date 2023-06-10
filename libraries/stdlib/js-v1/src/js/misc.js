/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors. 
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

Kotlin.compareTo = function (a, b) {
    var typeA = typeof a;
    if (typeA === "number") {
        if (typeof b === "number") {
            return Kotlin.doubleCompareTo(a, b);
        }
        return Kotlin.primitiveCompareTo(a, b);
    }
    if (typeA === "string" || typeA === "boolean") {
        return Kotlin.primitiveCompareTo(a, b);
    }
    return a.compareTo_11rb$(b);
};

Kotlin.primitiveCompareTo = function (a, b) {
    return a < b ? -1 : a > b ? 1 : 0;
};

Kotlin.doubleCompareTo = function (a, b) {
    if (a < b) return -1;
    if (a > b) return 1;

    if (a === b) {
        if (a !== 0) return 0;

        var ia = 1 / a;
        return ia === 1 / b ? 0 : (ia < 0 ? -1 : 1);
    }

    return a !== a ? (b !== b ? 0 : 1) : -1
};

Kotlin.charInc = function (konstue) {
    return Kotlin.toChar(konstue+1);
};

Kotlin.charDec = function (konstue) {
    return Kotlin.toChar(konstue-1);
};

Kotlin.imul = Math.imul || imul;

Kotlin.imulEmulated = imul;

function imul(a, b) {
    return ((a & 0xffff0000) * (b & 0xffff) + (a & 0xffff) * (b | 0)) | 0;
}

(function() {
    var buf = new ArrayBuffer(8);
    var bufFloat64 = new Float64Array(buf);
    var bufFloat32 = new Float32Array(buf);
    var bufInt32 = new Int32Array(buf);
    var lowIndex = 0;
    var highIndex = 1;

    bufFloat64[0] = -1; // bff00000_00000000
    if (bufInt32[lowIndex] !== 0) {
        lowIndex = 1;
        highIndex = 0;
    }

    Kotlin.doubleToBits = function(konstue) {
        return Kotlin.doubleToRawBits(isNaN(konstue) ? NaN : konstue);
    };

    Kotlin.doubleToRawBits = function(konstue) {
        bufFloat64[0] = konstue;
        return Kotlin.Long.fromBits(bufInt32[lowIndex], bufInt32[highIndex]);
    };

    Kotlin.doubleFromBits = function(konstue) {
        bufInt32[lowIndex] = konstue.low_;
        bufInt32[highIndex] = konstue.high_;
        return bufFloat64[0];
    };

    Kotlin.floatToBits = function(konstue) {
        return Kotlin.floatToRawBits(isNaN(konstue) ? NaN : konstue);
    };

    Kotlin.floatToRawBits = function(konstue) {
        bufFloat32[0] = konstue;
        return bufInt32[0];
    };

    Kotlin.floatFromBits = function(konstue) {
        bufInt32[0] = konstue;
        return bufFloat32[0];
    };

    // returns zero konstue for number with positive sign bit and non-zero konstue for number with negative sign bit.
    Kotlin.doubleSignBit = function(konstue) {
        bufFloat64[0] = konstue;
        return bufInt32[highIndex] & 0x80000000;
    };

    Kotlin.numberHashCode = function(obj) {
        if ((obj | 0) === obj) {
            return obj | 0;
        }
        else {
            bufFloat64[0] = obj;
            return (bufInt32[highIndex] * 31 | 0) + bufInt32[lowIndex] | 0;
        }
    }
})();

Kotlin.ensureNotNull = function(x) {
    return x != null ? x : Kotlin.throwNPE();
};
