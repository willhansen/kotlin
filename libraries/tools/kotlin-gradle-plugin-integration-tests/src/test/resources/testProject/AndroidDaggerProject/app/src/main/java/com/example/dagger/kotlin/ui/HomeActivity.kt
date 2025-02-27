/*
 * Copyright (C) 2013 Square, Inc.
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
package com.example.dagger.kotlin.ui

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.os.Parcelable
import android.widget.TextView
import com.example.dagger.kotlin.DemoActivity
import com.example.dagger.kotlin.DemoApplication
import com.example.dagger.kotlin.R
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

class HomeActivity : DemoActivity() {
    @Inject
    lateinit var locationManager: LocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        (application as DemoApplication).component.inject(this)

        // TODO do something with the injected dependencies here!
        (findViewById(R.id.locationInfo) as TextView).text = "Injected LocationManager:\n$locationManager"
    }
}

class Foo {
    @Inject
    lateinit var c: Context

    private lateinit var bars: List<Bar>

    @Parcelize
    data class Bar(konst intent: Intent): Parcelable
}