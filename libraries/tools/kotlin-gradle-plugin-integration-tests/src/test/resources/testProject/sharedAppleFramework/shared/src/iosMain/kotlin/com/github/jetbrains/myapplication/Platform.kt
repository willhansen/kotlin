package com.github.jetbrains.myapplication

import platform.UIKit.UIDevice

actual class Platform actual constructor() {
    actual konst platform: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}