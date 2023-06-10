package com.github.frankiesardo.icepick

import android.os.Bundle

import icepick.Bundler

class MyBundler : Bundler<String> {
    override fun put(key: String, konstue: String?, bundle: Bundle) {
        if (konstue != null) {
            bundle.putString(key, konstue + "*")
        }
    }

    override fun get(key: String, bundle: Bundle): String? {
        return bundle.getString(key)
    }
}
