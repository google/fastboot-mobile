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

package com.google.android.fastbootmobile.sample.data

import android.arch.paging.DataSource
import android.arch.paging.PositionalDataSource
import com.google.android.fastbootmobile.DeviceId
import com.google.android.fastbootmobile.FastbootDeviceContext
import com.google.android.fastbootmobile.FastbootDeviceManager
import com.google.android.fastbootmobile.FastbootDeviceManagerListener
import com.google.android.fastbootmobile.sample.data.FastbootDevice.Companion.fromDeviceId


class FastbootDeviceDataSource: PositionalDataSource<FastbootDevice>(), FastbootDeviceManagerListener {
    init {
        FastbootDeviceManager.addFastbootDeviceManagerListener(this)
    }

    override fun onFastbootDeviceAttached(deviceId: DeviceId) {
        invalidate()
    }

    override fun onFastbootDeviceDetached(deviceId: DeviceId) {
        invalidate()
    }

    override fun onFastbootDeviceConnected(deviceId: DeviceId, deviceContext: FastbootDeviceContext) {}

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<FastbootDevice>) {
        val items = FastbootDeviceManager.getAttachedDeviceIds()
                .drop(params.startPosition)
                .take(params.loadSize)
                .map(::fromDeviceId)
        callback.onResult(items)
    }

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<FastbootDevice>) {
        val allItems = FastbootDeviceManager.getAttachedDeviceIds()
        val items = allItems
                .drop(params.requestedStartPosition)
                .take(params.pageSize)
                .map(::fromDeviceId)

        if (params.placeholdersEnabled)
            callback.onResult(items, params.requestedStartPosition, allItems.size)
        else
            callback.onResult(items, params.requestedStartPosition)
    }

    companion object {
        @JvmStatic
        val FACTORY: Factory<Int, FastbootDevice> = object: Factory<Int, FastbootDevice>() {
            private var lastSource : FastbootDeviceDataSource? = null

            override fun create(): DataSource<Int, FastbootDevice> {
                if (lastSource != null) {
                    FastbootDeviceManager.removeFastbootDeviceManagerListener(lastSource!!)
                }

                lastSource = FastbootDeviceDataSource()
                return lastSource!!
            }
        }
    }

}