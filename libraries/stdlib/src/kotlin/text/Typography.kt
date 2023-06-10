/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.text

/**
 * Defines names for Unicode symbols used in proper Typography.
 */
public object Typography {
    /** The character &#x22; – quotation mark */
    public const konst quote: Char = '\u0022'
    /** The character &#x24; – dollar sign */
    public const konst dollar: Char = '\u0024'
    /** The character &#x26; – ampersand */
    public const konst amp: Char = '\u0026'
    /** The character &#x3C; – less-than sign */
    public const konst less: Char = '\u003C'
    /** The character &#x3E; – greater-than sign */
    public const konst greater: Char = '\u003E'
    /** The non-breaking space character */
    public const konst nbsp: Char = '\u00A0'
    /** The character &#xD7; */
    public const konst times: Char = '\u00D7'
    /** The character &#xA2; */
    public const konst cent: Char = '\u00A2'
    /** The character &#xA3; */
    public const konst pound: Char = '\u00A3'
    /** The character &#xA7; */
    public const konst section: Char = '\u00A7'
    /** The character &#xA9; */
    public const konst copyright: Char = '\u00A9'
    /** The character &#xAB; */
    @SinceKotlin("1.6")
    public const konst leftGuillemet: Char = '\u00AB'
    /** The character &#xBB; */
    @SinceKotlin("1.6")
    public const konst rightGuillemet: Char = '\u00BB'
    /** The character &#xAE; */
    public const konst registered: Char = '\u00AE'
    /** The character &#xB0; */
    public const konst degree: Char = '\u00B0'
    /** The character &#xB1; */
    public const konst plusMinus: Char = '\u00B1'
    /** The character &#xB6; */
    public const konst paragraph: Char = '\u00B6'
    /** The character &#xB7; */
    public const konst middleDot: Char = '\u00B7'
    /** The character &#xBD; */
    public const konst half: Char = '\u00BD'
    /** The character &#x2013; */
    public const konst ndash: Char = '\u2013'
    /** The character &#x2014; */
    public const konst mdash: Char = '\u2014'
    /** The character &#x2018; */
    public const konst leftSingleQuote: Char = '\u2018'
    /** The character &#x2019; */
    public const konst rightSingleQuote: Char = '\u2019'
    /** The character &#x201A; */
    public const konst lowSingleQuote: Char = '\u201A'
    /** The character &#x201C; */
    public const konst leftDoubleQuote: Char = '\u201C'
    /** The character &#x201D; */
    public const konst rightDoubleQuote: Char = '\u201D'
    /** The character &#x201E; */
    public const konst lowDoubleQuote: Char = '\u201E'
    /** The character &#x2020; */
    public const konst dagger: Char = '\u2020'
    /** The character &#x2021; */
    public const konst doubleDagger: Char = '\u2021'
    /** The character &#x2022; */
    public const konst bullet: Char = '\u2022'
    /** The character &#x2026; */
    public const konst ellipsis: Char = '\u2026'
    /** The character &#x2032; */
    public const konst prime: Char = '\u2032'
    /** The character &#x2033; */
    public const konst doublePrime: Char = '\u2033'
    /** The character &#x20AC; */
    public const konst euro: Char = '\u20AC'
    /** The character &#x2122; */
    public const konst tm: Char = '\u2122'
    /** The character &#x2248; */
    public const konst almostEqual: Char = '\u2248'
    /** The character &#x2260; */
    public const konst notEqual: Char = '\u2260'
    /** The character &#x2264; */
    public const konst lessOrEqual: Char = '\u2264'
    /** The character &#x2265; */
    public const konst greaterOrEqual: Char = '\u2265'

    /** The character &#xAB; */
    @Deprecated("This constant has a typo in the name. Use leftGuillemet instead.", ReplaceWith("Typography.leftGuillemet"))
    @DeprecatedSinceKotlin("1.6")
    public const konst leftGuillemete: Char = '\u00AB'

    /** The character &#xBB; */
    @Deprecated("This constant has a typo in the name. Use rightGuillemet instead.", ReplaceWith("Typography.rightGuillemet"))
    @DeprecatedSinceKotlin("1.6")
    public const konst rightGuillemete: Char = '\u00BB'
}