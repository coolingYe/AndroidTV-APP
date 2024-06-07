package com.zee.setting.views.pickerview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.zee.setting.R

class TimeView : ConstraintLayout {

    private lateinit var currentText: TextView
    private lateinit var signUpView: View
    private lateinit var signDownView: View
    private var typeMode: Int = TYPE_MODE_HOUR
    private var minValue = 0
    private var maxValue = 23
    private var currentValue = 0
    private var checked = false
    private var hasStandard = false

    companion object {
        const val TYPE_MODE_HOUR = 0
        const val TYPE_MODE_MINUTE = 1
    }

    constructor(context: Context?) : super(context!!) {
        TimeView(context, null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {
        initView(context, attrs)
    }

    @SuppressLint("Recycle", "CustomViewStyleable")
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!,
        attrs,
        defStyleAttr
    )
    @SuppressLint("CustomViewStyleable", "Recycle")
    private fun initView(context: Context, attrs: AttributeSet?) {
        LayoutInflater.from(context).inflate(R.layout.layout_time_view, this)
        val typeArray = context.obtainStyledAttributes(attrs, R.styleable.TimeSwitchView)
        typeMode = typeArray.getInt(R.styleable.TimeSwitchView_typeMode, TYPE_MODE_HOUR)
        isFocusable = true

        currentText = findViewById(R.id.time_text)
        signUpView = findViewById(R.id.sign_up)
        signDownView = findViewById(R.id.sign_down)
        signUpView.isVisible = false
        signDownView.isVisible = false

        setOnFocusChangeListener { _, hasFocus ->
            run {
                if (hasFocus) {
                    currentText.setTextColor(Color.WHITE)
                    currentText.setBackgroundResource(R.drawable.shape_rectange_gradient_6a6_ae8)
                } else {
                    currentText.setTextColor(Color.BLACK)
                    currentText.setBackgroundResource(R.drawable.shape_rectangle_white)
                }
            }
        }

        signDownView = findViewById(R.id.sign_down)
        when (typeMode) {
            TYPE_MODE_HOUR -> {
                maxValue = 23
            }
            TYPE_MODE_MINUTE -> {
                maxValue = if (hasStandard) 59 else 50
            }
        }
        currentText.text = currentValue.toString()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            val keyCode = event.keyCode
            return setCurrentItem(keyCode)
        }
        return false
    }

    private fun setCurrentItem(keyCode: Int): Boolean {
        when {
            keyCode == KeyEvent.KEYCODE_DPAD_CENTER -> {
                checked = !checked
                signUpView.isVisible = checked && currentValue != minValue
                signDownView.isVisible = checked && currentValue != maxValue
                return true
            }
            keyCode == KeyEvent.KEYCODE_DPAD_LEFT -> {
                return checked
            }
            keyCode == KeyEvent.KEYCODE_DPAD_RIGHT -> {
                return checked
            }
            keyCode == KeyEvent.KEYCODE_DPAD_UP && checked -> {
                setCurrentUpItem()
                currentText.text = currentValue.toString()
                return true
            }
            keyCode == KeyEvent.KEYCODE_DPAD_DOWN && checked -> {
                setCurrentDownItem()
                currentText.text = currentValue.toString()
                return true
            }
        }
        return false
    }

    private fun setCurrentUpItem() {
        if (typeMode == TYPE_MODE_HOUR) {
            if (currentValue != 0) {
                currentValue -= 1
            }
        } else {
            if (currentValue != 0 && hasStandard) {
                currentValue -= 1
            } else if (currentValue != 0) {
                currentValue -= 10
            }
        }
        signUpView.isVisible = currentValue != 0
        signDownView.isVisible = currentValue != maxValue
    }

    private fun setCurrentDownItem() {
        if (typeMode == TYPE_MODE_HOUR) {
            if (currentValue != 23) {
                currentValue += 1
            }
            signDownView.isVisible = currentValue != 23
        } else {
            if (currentValue != 59 && hasStandard) {
                currentValue += 1
                signDownView.isVisible = currentValue != 59
            } else if (currentValue != 50 && !hasStandard) {
                currentValue += 10
                signDownView.isVisible = currentValue != 50
            }
        }
        signUpView.isVisible = currentValue != 0
    }

    private fun setHasStandard(hasStandard: Boolean) {
        this.hasStandard = hasStandard
    }

    fun setCurrentValue(currentValue: Int) {
        this.currentValue = currentValue
        currentText.text = this.currentValue.toString()
    }

    fun getCurrentValue(): Int {
        return currentValue
    }

    fun setCyclic(cyclic: Boolean) {
        //TODO
    }
}