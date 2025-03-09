package com.hebit.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Main application class for Hebit
 * HiltAndroidApp annotation triggers Hilt's code generation
 */
@HiltAndroidApp
class HebitApplication : Application()
