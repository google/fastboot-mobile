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

import android.arch.paging.PagedListAdapter
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.fastbootmobile.sample.databinding.ItemFastbootDeviceBinding
import com.google.android.fastbootmobile.sample.ui.OnFastbootDeviceItemClickListener

class FastbootDeviceAdapter: OnFastbootDeviceItemClickListener,
        PagedListAdapter<FastbootDevice, FastbootDeviceAdapter.ViewHolder>(FASTBOOT_DEVICE_COMPARATOR) {
    private var listeners: MutableList<OnFastbootDeviceItemClickListener> = mutableListOf()

    class ViewHolder(private val binding: ItemFastbootDeviceBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(device: FastbootDevice?, listener: OnFastbootDeviceItemClickListener?) {
            binding.device = device
            binding.onClickListener = listener
            binding.executePendingBindings()
        }
    }

    override fun onFastbootDeviceItemClick(device: FastbootDevice, view: View) {
        listeners.forEach { it.onFastbootDeviceItemClick(device, view) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemFastbootDeviceBinding
                .inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), this)
    }

    fun addOnFastbootDeviceItemClickListener(listener: OnFastbootDeviceItemClickListener) {
        listeners.add(listener)
    }

    fun removeOnFastbootDeviceItemClickListener(listener: OnFastbootDeviceItemClickListener) {
        listeners.remove(listener)
    }

    companion object {
        @JvmStatic
        val FASTBOOT_DEVICE_COMPARATOR = object : DiffUtil.ItemCallback<FastbootDevice>() {
            override fun areItemsTheSame(oldItem: FastbootDevice?, newItem: FastbootDevice?): Boolean {
                return oldItem!!.deviceId == newItem!!.deviceId
            }

            override fun areContentsTheSame(oldItem: FastbootDevice?, newItem: FastbootDevice?): Boolean {
                return oldItem!! == newItem!!
            }

        }
    }
}
