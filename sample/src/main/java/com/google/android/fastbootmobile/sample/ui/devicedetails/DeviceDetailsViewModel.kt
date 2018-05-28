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

package com.google.android.fastbootmobile.sample.ui.devicedetails

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.google.android.fastbootmobile.DeviceId
import com.google.android.fastbootmobile.FastbootDeviceContext
import com.google.android.fastbootmobile.FastbootDeviceManager
import com.google.android.fastbootmobile.FastbootDeviceManagerListener
import com.google.android.fastbootmobile.sample.data.FastbootDevice

class DeviceDetailsViewModel : ViewModel(), FastbootDeviceManagerListener {
    var fastbootDevice: MutableLiveData<FastbootDevice?> = MutableLiveData()
    private var deviceContext: FastbootDeviceContext? = null

    init {
        FastbootDeviceManager.addFastbootDeviceManagerListener(this)
    }

    fun connectToDevice(deviceId: DeviceId) {
        fastbootDevice.value = null
        deviceContext?.close()
        FastbootDeviceManager.connectToDevice(deviceId)
    }

    override fun onFastbootDeviceAttached(deviceId: DeviceId) {}

    override fun onFastbootDeviceDetached(deviceId: DeviceId) {
        if (fastbootDevice.value?.deviceId != deviceId) return
        deviceContext?.close()
        deviceContext = null
    }

    override fun onFastbootDeviceConnected(deviceId: DeviceId, deviceContext: FastbootDeviceContext) {
        this.deviceContext = deviceContext
        fastbootDevice.value = FastbootDevice.fromFastbootDeviceContext(deviceId, deviceContext)
    }

    override fun onCleared() {
        super.onCleared()
        FastbootDeviceManager.removeFastbootDeviceManagerListener(this)
        deviceContext?.close()
        deviceContext = null
    }
}
