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

import com.google.android.fastbootmobile.DeviceId
import com.google.android.fastbootmobile.FastbootCommand
import com.google.android.fastbootmobile.FastbootDeviceContext

data class FastbootDevice(val deviceId: DeviceId, val serialNumber: String, val currentSlot: String) {
    companion object {
        fun fromDeviceId(deviceId: DeviceId) = FastbootDevice(deviceId, "", "")

        fun fromFastbootDeviceContext(deviceId: DeviceId, deviceContext: FastbootDeviceContext): FastbootDevice =
                FastbootDevice(
                        deviceId,
                        deviceContext.sendCommand(FastbootCommand.getVar("serialno")).data,
                        deviceContext.sendCommand(FastbootCommand.getVar("current-slot")).data)
    }
}
