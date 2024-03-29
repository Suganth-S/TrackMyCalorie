package com.suganth.trackmycalorie.ui.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.suganth.trackmycalorie.R
import com.suganth.trackmycalorie.ui.viewmodels.MainViewModel
import com.suganth.trackmycalorie.ui.viewmodels.StatisticsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StatisticsFragment:Fragment(R.layout.fragment_statistics) {

    private val viewModel: StatisticsViewModel by viewModels()
}