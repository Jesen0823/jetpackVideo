package com.jesen.cod.libcommon.view

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import androidx.appcompat.widget.AppCompatEditText

class ViEditTextView : AppCompatEditText {
    private lateinit var keyEvent: OnBackKeyEvent

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(
        context, attrs
    )

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    )

    override fun dispatchKeyEventPreIme(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK) {
            if (keyEvent.onKeyEvent()) {
                return true
            }
        }
        return super.dispatchKeyEventPreIme(event)
    }

    fun setOnBackKeyEventListener(event: OnBackKeyEvent) {
        keyEvent = event
    }

    interface OnBackKeyEvent {
        fun onKeyEvent(): Boolean
    }
}