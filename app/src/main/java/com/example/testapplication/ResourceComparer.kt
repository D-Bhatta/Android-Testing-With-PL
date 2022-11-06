// ResourceComparer.kt
package com.example.testapplication

import android.content.Context

class ResourceComparer {

    fun isStringEqual(context: Context, resId: Int, string: String): Boolean {
        return context.getString(resId) == string
    }
}