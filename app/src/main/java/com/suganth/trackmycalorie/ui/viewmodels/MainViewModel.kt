package com.suganth.trackmycalorie.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.suganth.trackmycalorie.repositories.MainRepository
import javax.inject.Inject

/**
 * Usually we cannot create instance of a viewModel that easily, and also dagger cant do that easily
 * because when we want to pass viewmodel then you probably know we need to create a viewModelFactory
 * for that in the old dagger that was super complicated to inject stuff into view models that needed a
 * huge workaround , but using dagger hilt it becomes much easier, becoz dagger hilt will manage all that view
 * model factory stuff and that injection stuff behind the scenes for us and the thing we need to do here is not
 * @Inject instead we need to use @ViewModelInject
 */
class MainViewModel @ViewModelInject constructor(
    val mainRepository: MainRepository
): ViewModel() {

}