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

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.fastbootmobile.sample.R
import com.google.android.fastbootmobile.sample.databinding.FragmentDeviceDetailsBinding
import com.google.android.fastbootmobile.sample.ui.devicedetails.DeviceDetailsFragmentArgs

class DeviceDetailsFragment : Fragment() {
    private lateinit var viewModel: DeviceDetailsViewModel
    private lateinit var binding: FragmentDeviceDetailsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_device_details, container, false)
        binding = FragmentDeviceDetailsBinding.bind(view)

        viewModel = ViewModelProviders.of(this).get(DeviceDetailsViewModel::class.java)
        viewModel.fastbootDevice.observe(this, Observer {
            binding.device = it
        })

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val deviceId = DeviceDetailsFragmentArgs.fromBundle(arguments).deviceId
        viewModel.connectToDevice(deviceId)
    }
}
