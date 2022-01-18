package ru.myproevent.ui.fragments.authorization

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import moxy.ktx.moxyPresenter
import ru.myproevent.ProEventApp
import ru.myproevent.R
import ru.myproevent.databinding.FragmentNewPasswordBinding
import ru.myproevent.domain.utils.pxValue
import ru.myproevent.ui.fragments.BaseMvpFragment
import ru.myproevent.ui.presenters.authorization.new_password.NewPasswordPresenter
import ru.myproevent.ui.presenters.authorization.new_password.NewPasswordView
import ru.myproevent.ui.presenters.main.BottomNavigation
import ru.myproevent.ui.presenters.main.RouterProvider
import ru.myproevent.ui.presenters.main.Tab
import ru.myproevent.ui.views.KeyboardAwareTextInputEditText

class NewPasswordFragment :
    BaseMvpFragment<FragmentNewPasswordBinding>(FragmentNewPasswordBinding::inflate),
    NewPasswordView {

    private inner class SingleDigitEditKeyListener(
        private val prevViewInRow: KeyboardAwareTextInputEditText?,
        private val nextViewInRow: KeyboardAwareTextInputEditText?
    ) : View.OnKeyListener {
        override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
            if (event.action == KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_ENTER -> {
                        nextViewInRow?.requestFocus() ?: return false
                        return true
                    }
                    KeyEvent.KEYCODE_DEL -> {
                        prevViewInRow?.requestFocus()
                        prevViewInRow?.setSelection(prevViewInRow.length())
                        return false
                    }
                }
            }
            return false
        }
    }

    private inner class SingleDigitEditTextWatcher(
        private val nextViewInRow: KeyboardAwareTextInputEditText?
    ) : TextWatcher {
        private var lastValue = ""
        private var isFirstValue = true
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        override fun beforeTextChanged(
            s: CharSequence,
            start: Int,
            count: Int,
            after: Int
        ) {
            lastValue = s.toString()
        }

        override fun afterTextChanged(s: Editable) {
            if (isFirstValue) {
                isFirstValue = false
                nextViewInRow?.requestFocus()
            }
            var newValue = ""
            if (s.toString().length > 1) {
                newValue = if (s[0].toString() == lastValue) {
                    s.subSequence(1, 2).toString()
                } else {
                    s.subSequence(0, 1).toString()
                }
                s.replace(0, s.length, lastValue)
            }
            if (newValue.isNotEmpty()) {
                nextViewInRow?.requestFocus()
                nextViewInRow?.text = SpannableStringBuilder(newValue)
            }

            presenter.codeEdit()
        }
    }

    private val email: String by lazy {
        requireArguments().getString(EMAIL_ARG)!!
    }

    private val numberStringBuilder = StringBuilder()

    private fun getVerificationCode() = with(binding) {
        numberStringBuilder.clear()
        numberStringBuilder.append(digit1Edit.text)
        numberStringBuilder.append(digit2Edit.text)
        numberStringBuilder.append(digit3Edit.text)
        numberStringBuilder.append(digit4Edit.text)
        return@with numberStringBuilder.toString()
    }

    private fun setLayoutParams() = with(binding) {
        body.post {
            val availableHeight = root.height

            if (ohNowIRemember.lineCount > 1 || authorize.lineCount > 1) {
                bottomOptionsContainer.orientation = LinearLayout.VERTICAL
                bottomOptionsHorizontalSeparator.visibility = View.GONE
            }

            bodySpace.layoutParams = bodySpace.layoutParams.apply { height = availableHeight }
            body.post {
                val difference = body.height - availableHeight
                if (difference > 0) {
                    logo.isVisible = false
                }
                if (difference > pxValue(80f + 48f)) {
                    formTitle.isVisible = false
                }
            }
        }
    }

    override val presenter by moxyPresenter {
        NewPasswordPresenter((parentFragment as RouterProvider).router).apply {
            ProEventApp.instance.appComponent.inject(this)
        }
    }

    companion object {
        const val EMAIL_ARG = "EMAIL"
        fun newInstance(email: String) = NewPasswordFragment().apply {
            arguments =
                Bundle().apply { putString(EMAIL_ARG, email) }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)
        setLayoutParams()
        codeExplanation.text =
            String.format(getString(R.string.code_explanation_text), email)
        // TODO: отрефакторить - вынести это в кастомные вьюхи
        digit1Edit.selectionChangedListener =
            { _, _ -> digit1Edit.setSelection(digit1Edit.length()) }
        digit1Edit.addTextChangedListener(SingleDigitEditTextWatcher(digit2Edit))
        digit2Edit.selectionChangedListener =
            { _, _ -> digit2Edit.setSelection(digit2Edit.length()) }
        digit2Edit.addTextChangedListener(SingleDigitEditTextWatcher(digit3Edit))
        digit3Edit.selectionChangedListener =
            { _, _ -> digit3Edit.setSelection(digit3Edit.length()) }
        digit3Edit.addTextChangedListener(SingleDigitEditTextWatcher(digit4Edit))
        digit4Edit.selectionChangedListener =
            { _, _ -> digit4Edit.setSelection(digit4Edit.length()) }
        digit4Edit.addTextChangedListener(SingleDigitEditTextWatcher(null))
        digit4Edit.addTextChangedListener {
            if (it.toString().isNotEmpty()) {
                digit4Edit.hideKeyBoard()
            }
        }
        digit1Edit.setOnKeyListener(SingleDigitEditKeyListener(null, digit2Edit))
        digit2Edit.setOnKeyListener(SingleDigitEditKeyListener(digit1Edit, digit3Edit))
        digit3Edit.setOnKeyListener(SingleDigitEditKeyListener(digit2Edit, digit4Edit))
        digit4Edit.setOnKeyListener(SingleDigitEditKeyListener(digit3Edit, null))
        refreshCode.setOnClickListener { presenter.refreshCode(email) }
        continueRegistration.setOnClickListener {
            presenter.setNewPassword(
                code =  getVerificationCode(),
                email =  email,
                password =  passwordEdit.text.toString(),
                confirmedPassword = passwordConfirmEdit.text.toString(),
                rememberPassword = rememberMeCheckbox.isChecked
            )
        }
        back.setOnClickListener { presenter.onBackPressed() }
        bottomOptionsContainer.setOnClickListener { presenter.authorize() }
    }

    override fun showCodeErrorMessage(message: String?) = with(binding) {
        val colorStates = arrayOf(
            intArrayOf(android.R.attr.state_enabled),
            intArrayOf(-android.R.attr.state_enabled),
            intArrayOf(android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_checked),
            intArrayOf(android.R.attr.state_pressed),
            intArrayOf(-android.R.attr.state_pressed),
            intArrayOf(android.R.attr.state_focused),
            intArrayOf(-android.R.attr.state_focused),
        )

        val defaultColors = intArrayOf(
            resources.getColor(R.color.ProEvent_blue_300),
            resources.getColor(R.color.ProEvent_blue_300),
            resources.getColor(R.color.ProEvent_blue_300),
            resources.getColor(R.color.ProEvent_blue_300),
            resources.getColor(R.color.ProEvent_blue_300),
            resources.getColor(R.color.ProEvent_blue_300),
            resources.getColor(R.color.ProEvent_blue_300),
            resources.getColor(R.color.ProEvent_blue_300)
        )

        if (message.isNullOrBlank()) {
            errorMessage.isVisible = false
            digit1.setBoxStrokeColorStateList(ColorStateList(colorStates, defaultColors))
            digit2.setBoxStrokeColorStateList(ColorStateList(colorStates, defaultColors))
            digit3.setBoxStrokeColorStateList(ColorStateList(colorStates, defaultColors))
            digit4.setBoxStrokeColorStateList(ColorStateList(colorStates, defaultColors))
            return@with
        }
        errorMessage.text = message
        errorMessage.isVisible = true

        val errorColors = intArrayOf(
            Color.parseColor("#FF6A4D"),
            Color.parseColor("#FF6A4D"),
            Color.parseColor("#FF6A4D"),
            Color.parseColor("#FF6A4D"),
            Color.parseColor("#FF6A4D"),
            Color.parseColor("#FF6A4D"),
            Color.parseColor("#FF6A4D"),
            Color.parseColor("#FF6A4D")
        )

        digit1.setBoxStrokeColorStateList(ColorStateList(colorStates, errorColors))
        digit2.setBoxStrokeColorStateList(ColorStateList(colorStates, errorColors))
        digit3.setBoxStrokeColorStateList(ColorStateList(colorStates, errorColors))
        digit4.setBoxStrokeColorStateList(ColorStateList(colorStates, errorColors))
    }

    override fun showPasswordConfirmErrorMessage(message: String?) = with(binding) {
        if (message.isNullOrBlank()) {
            passwordConfirmErrorMessage.isVisible = false
            return@with
        }
        passwordConfirmErrorMessage.text = message
        passwordConfirmErrorMessage.isVisible = true
    }

    override fun finishAuthorization() = (requireActivity() as BottomNavigation).openTab(Tab.HOME)

}