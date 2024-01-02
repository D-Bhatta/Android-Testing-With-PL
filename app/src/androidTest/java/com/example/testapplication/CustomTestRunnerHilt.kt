// CustomTestRunnerHilt
package com.example.testapplication

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

class CustomTestRunnerHilt : AndroidJUnitRunner() {

    override fun newApplication(
        cl: ClassLoader?, className: String?, context: Context?
    ): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
}
