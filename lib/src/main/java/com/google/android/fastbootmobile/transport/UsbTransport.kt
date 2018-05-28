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

package com.google.android.fastbootmobile.transport

import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface

internal class UsbTransport(private val fastbootInterface: UsbInterface,
                   private val connection: UsbDeviceConnection) : Transport {
    override var isConnected: Boolean = false
    private lateinit var inEndpoint: UsbEndpoint
    private lateinit var outEndpoint: UsbEndpoint

    init {
        for (i in 0 until fastbootInterface.endpointCount) {
            val e1 = fastbootInterface.getEndpoint(i)

            if (e1.direction == UsbConstants.USB_DIR_IN) {
                inEndpoint = e1
            } else {
                outEndpoint = e1
            }
        }

        if (!this::inEndpoint.isInitialized)
            throw RuntimeException("No endpoint found for input.")
        if (!this::outEndpoint.isInitialized)
            throw RuntimeException("No endpoint found for output.")
    }

    override fun connect(force: Boolean) {
        connection.claimInterface(fastbootInterface, force)
        this.isConnected = true
    }

    override fun disconnect() {
        if (!isConnected) return
        connection.releaseInterface(fastbootInterface)
        isConnected = false
    }

    override fun close() {
        disconnect()
        connection.close()
    }

    override fun send(buffer: ByteArray, timeout: Int) {
        connection.bulkTransfer(outEndpoint, buffer, buffer.size, timeout)
    }

    override fun receive(buffer: ByteArray, timeout: Int) {
        connection.bulkTransfer(inEndpoint, buffer, buffer.size, timeout)
    }

}