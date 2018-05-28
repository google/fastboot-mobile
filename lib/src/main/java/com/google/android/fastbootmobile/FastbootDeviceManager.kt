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

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbInterface
import com.google.android.fastbootmobile.transport.UsbTransport
import java.lang.ref.WeakReference

typealias DeviceId = String

object FastbootDeviceManager {
    private const val USB_CLASS = 0xff
    private const val USB_SUBCLASS = 0x42
    private const val USB_PROTOCOL = 0x03

    private val connectedDevices = HashMap<DeviceId, FastbootDeviceContext>()
    private val usbDeviceManager: UsbDeviceManager = UsbDeviceManager(
            WeakReference(FastbootMobile.getApplicationContext()))
    private val listeners = ArrayList<FastbootDeviceManagerListener>()

    private val usbDeviceManagerListener = object : UsbDeviceManagerListener {
        override fun filterDevice(device: UsbDevice): Boolean =
                this@FastbootDeviceManager.filterDevice(device)

        @Synchronized
        override fun onUsbDeviceAttached(device: UsbDevice) {
            listeners.forEach {
                it.onFastbootDeviceAttached(device.serialNumber)
            }
        }

        @Synchronized
        override fun onUsbDeviceDetached(device: UsbDevice) {
            if (connectedDevices.containsKey(device.serialNumber)) {
                connectedDevices[device.serialNumber]?.close()
            }
            connectedDevices.remove(device.serialNumber)

            listeners.forEach {
                it.onFastbootDeviceDetached(device.serialNumber)
            }
        }

        @Synchronized
        override fun onUsbDeviceConnected(device: UsbDevice,
                                          connection: UsbDeviceConnection) {
            val deviceContext = FastbootDeviceContext(
                    UsbTransport(findFastbootInterface(device)!!, connection))

            connectedDevices[device.serialNumber]?.close()
            connectedDevices[device.serialNumber] = deviceContext
            listeners.forEach {
                it.onFastbootDeviceConnected(device.serialNumber, deviceContext)
            }
        }
    }

    private fun filterDevice(device: UsbDevice): Boolean {
        if (device.deviceClass == USB_CLASS &&
                device.deviceSubclass == USB_SUBCLASS &&
                device.deviceProtocol == USB_PROTOCOL) {
            return true
        }

        return findFastbootInterface(device) != null
    }

    @Synchronized
    fun addFastbootDeviceManagerListener(
            listener: FastbootDeviceManagerListener) {
        listeners.add(listener)

        if (listeners.size == 1)
            usbDeviceManager.addUsbDeviceManagerListener(
                    usbDeviceManagerListener)
    }

    @Synchronized
    fun removeFastbootDeviceManagerListener(
            listener: FastbootDeviceManagerListener) {
        listeners.remove(listener)

        if (listeners.size == 0)
            usbDeviceManager.removeUsbDeviceManagerListener(
                    usbDeviceManagerListener)
    }

    @Synchronized
    fun connectToDevice(deviceId: DeviceId) {
        val device = usbDeviceManager.getDevices().values
                .filter(::filterDevice).firstOrNull { it.serialNumber == deviceId }
        if (device != null)
            usbDeviceManager.connectToDevice(device)
    }

    @Synchronized
    fun getAttachedDeviceIds(): List<DeviceId> {
        return usbDeviceManager.getDevices().values
                .filter(::filterDevice).map { it.serialNumber }.toList()
    }

    @Synchronized
    fun getConnectedDeviceIds(): List<DeviceId> {
        return connectedDevices.keys.toList()
    }

    @Synchronized
    fun getDeviceContext(
            deviceId: DeviceId): Pair<DeviceId, FastbootDeviceContext>? {
        return connectedDevices.entries.firstOrNull {
            it.key == deviceId
        }?.toPair()
    }

    private fun findFastbootInterface(device: UsbDevice): UsbInterface? {
        for (i: Int in 0 until device.interfaceCount) {
            val deviceInterface = device.getInterface(i)
            if (deviceInterface.interfaceClass == USB_CLASS &&
                    deviceInterface.interfaceSubclass == USB_SUBCLASS &&
                    deviceInterface.interfaceProtocol == USB_PROTOCOL) {
                return deviceInterface
            }
        }
        return null
    }
}
