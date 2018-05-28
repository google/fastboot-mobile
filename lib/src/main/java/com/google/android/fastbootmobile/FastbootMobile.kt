/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.fastbootmobile

import android.content.Context
import java.lang.ref.WeakReference

internal object FastbootMobile {
    private lateinit var applicationContext: WeakReference<Context>

    @JvmStatic
    @Synchronized
    fun initialize(context: Context) {
        applicationContext = WeakReference(context)
    }

    @JvmStatic
    fun getApplicationContext(): Context {
        if (applicationContext.get() == null) {
            throw RuntimeException("FastbootMobile not initialized.")
        }
        return applicationContext.get()!!
    }
}
