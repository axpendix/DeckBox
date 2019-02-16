package com.r0adkll.deckbuilder.arch.ui.components.delegates

import android.os.Bundle

interface FragmentDelegate {

    fun onActivityCreated(savedInstanceState: Bundle?)
    fun onSaveInstanceState(outState: Bundle)
    fun onResume()
    fun onStart()
    fun onStop()
    fun onPause()
    fun onDestroy()
}