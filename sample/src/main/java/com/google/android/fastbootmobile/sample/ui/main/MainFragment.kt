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

package com.google.android.fastbootmobile.sample.ui.main

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.google.android.fastbootmobile.sample.R
import com.google.android.fastbootmobile.sample.data.FastbootDevice
import com.google.android.fastbootmobile.sample.data.FastbootDeviceAdapter
import com.google.android.fastbootmobile.sample.ui.OnFastbootDeviceItemClickListener
import com.google.android.fastbootmobile.sample.ui.main.MainFragmentDirections

class MainFragment : Fragment(), OnFastbootDeviceItemClickListener {
    private lateinit var viewModel: MainViewModel
    private lateinit var rvAttachedDevices: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.main_fragment, container, false)

        rvAttachedDevices = view.findViewById(R.id.rv_attached_devices)
        rvAttachedDevices.setHasFixedSize(true)
        rvAttachedDevices.layoutManager = LinearLayoutManager(context)

        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        val fastbootDeviceAdapter = FastbootDeviceAdapter()
        fastbootDeviceAdapter.addOnFastbootDeviceItemClickListener(this)
        rvAttachedDevices.adapter = fastbootDeviceAdapter

        viewModel.fastbootDevices.observe(this, Observer {
            fastbootDeviceAdapter.submitList(it)
        })

        return view
    }

    override fun onFastbootDeviceItemClick(device: FastbootDevice, view: View) = showDeviceDetails(device, view)

    private fun showDeviceDetails(device: FastbootDevice, view: View) {
        val connectAction = MainFragmentDirections.connectAction(device.deviceId)
        view.findNavController().navigate(connectAction)
    }
}
