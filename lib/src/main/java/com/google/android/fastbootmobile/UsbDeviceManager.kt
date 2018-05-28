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

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log
import java.lang.ref.WeakReference

internal class UsbDeviceManager(private val context: WeakReference<Context>) {
    private val usbActionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return
            when (intent.action) {
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> listeners.forEach {
                    val device: UsbDevice =
                            intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                    if (it.filterDevice(device)) it.onUsbDeviceAttached(device)
                }

                UsbManager.ACTION_USB_DEVICE_DETACHED -> listeners.forEach {
                    val device: UsbDevice =
                            intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                    if (it.filterDevice(device)) it.onUsbDeviceDetached(device)
                }
            }
        }
    }

    private val usbPermissionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null || ACTION_USB_PERMISSION != intent.action) return

            synchronized(this) {
                val device: UsbDevice =
                        intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)

                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED,
                                false)) {
                    connectToDeviceInternal(device)
                } else {
                    Log.d(TAG, "Permission denied for device $device")
                }
            }
        }
    }

    private val listeners = ArrayList<UsbDeviceManagerListener>()
    private var usbManager: UsbManager =
            context.get()?.getSystemService(Context.USB_SERVICE) as UsbManager

    init {
        val permissionFilter = IntentFilter(ACTION_USB_PERMISSION)
        context.get()?.registerReceiver(usbPermissionReceiver, permissionFilter)
    }

    fun addUsbDeviceManagerListener(listener: UsbDeviceManagerListener) {
        listeners.add(listener)

        if (listeners.size == 1) {
            val usbActionFilter =
                    IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            usbActionFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
            context.get()?.registerReceiver(usbActionReceiver, usbActionFilter)
        }
    }

    fun removeUsbDeviceManagerListener(listener: UsbDeviceManagerListener) {
        listeners.remove(listener)

        if (listeners.size == 0) {
            context.get()?.unregisterReceiver(usbActionReceiver)
        }
    }

    fun getDevices(): Map<String, UsbDevice> = usbManager.deviceList

    fun connectToDevice(device: UsbDevice) {
        val permissionIntent = PendingIntent.getBroadcast(context.get(), 0,
                Intent(ACTION_USB_PERMISSION), 0)

        if (usbManager.hasPermission(device)) connectToDeviceInternal(device)
        else usbManager.requestPermission(device, permissionIntent)
    }

    private fun connectToDeviceInternal(device: UsbDevice) {
        val connection = usbManager.openDevice(device)
        listeners.forEach {
            if (it.filterDevice(device)) it.onUsbDeviceConnected(device,
                    connection)
        }
    }

    companion object {
        @JvmStatic
        val TAG: String = UsbDeviceManager::class.java.name
        @JvmStatic
        val ACTION_USB_PERMISSION = "com.google.android.fastbootmobile.USB_PERMISSION"
    }
}
