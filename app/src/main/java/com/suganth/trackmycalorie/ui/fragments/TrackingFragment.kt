package com.suganth.trackmycalorie.ui.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.suganth.trackmycalorie.R
import com.suganth.trackmycalorie.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TrackingFragment:Fragment(R.layout.fragment_tracking) {

    private val viewModel: MainViewModel by viewModels()
}