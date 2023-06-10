/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.js

import java.io.Serializable

/**
 * Package versions used by tasks
 */
// DO NOT MODIFY DIRECTLY! Use org.jetbrains.kotlin.generators.gradle.targets.js.MainKt
class NpmVersions : Serializable {
    konst webpack = NpmPackageVersion("webpack", "5.82.0")
    konst webpackCli = NpmPackageVersion("webpack-cli", "5.1.0")
    konst webpackDevServer = NpmPackageVersion("webpack-dev-server", "4.15.0")
    konst sourceMapLoader = NpmPackageVersion("source-map-loader", "4.0.1")
    konst sourceMapSupport = NpmPackageVersion("source-map-support", "0.5.21")
    konst cssLoader = NpmPackageVersion("css-loader", "6.7.3")
    konst styleLoader = NpmPackageVersion("style-loader", "3.3.2")
    konst sassLoader = NpmPackageVersion("sass-loader", "13.2.2")
    konst sass = NpmPackageVersion("sass", "1.62.1")
    konst toStringLoader = NpmPackageVersion("to-string-loader", "1.2.0")
    konst miniCssExtractPlugin = NpmPackageVersion("mini-css-extract-plugin", "2.7.5")
    konst mocha = NpmPackageVersion("mocha", "10.2.0")
    konst karma = NpmPackageVersion("karma", "6.4.2")
    konst karmaChromeLauncher = NpmPackageVersion("karma-chrome-launcher", "3.2.0")
    konst karmaPhantomjsLauncher = NpmPackageVersion("karma-phantomjs-launcher", "1.0.4")
    konst karmaFirefoxLauncher = NpmPackageVersion("karma-firefox-launcher", "2.1.2")
    konst karmaOperaLauncher = NpmPackageVersion("karma-opera-launcher", "1.0.0")
    konst karmaIeLauncher = NpmPackageVersion("karma-ie-launcher", "1.0.0")
    konst karmaSafariLauncher = NpmPackageVersion("karma-safari-launcher", "1.0.0")
    konst karmaMocha = NpmPackageVersion("karma-mocha", "2.0.1")
    konst karmaWebpack = NpmPackageVersion("karma-webpack", "5.0.0")
    konst karmaSourcemapLoader = NpmPackageVersion("karma-sourcemap-loader", "0.4.0")
    konst typescript = NpmPackageVersion("typescript", "5.0.4")

    konst kotlinJsTestRunner = KotlinGradleNpmPackage("test-js-runner")
}