package com.vremea.romaniei.util

import android.content.Context
import androidx.annotation.StringRes

/**
 * A way for ViewModels to emit text without depending on Android Context.
 * UI layer calls .asString(context) to resolve.
 */
sealed class UiText {
    data class StringResource(@StringRes val resId: Int, val args: List<Any> = emptyList()) : UiText()
    data class DynamicString(val value: String) : UiText()

    fun asString(context: Context): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> {
                if (args.isEmpty()) context.getString(resId)
                else context.getString(resId, *args.toTypedArray())
            }
        }
    }
}
