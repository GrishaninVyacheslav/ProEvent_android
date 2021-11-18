package ru.myproevent.ui.fragments.authorization

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import moxy.ktx.moxyPresenter
import ru.myproevent.ProEventApp
import ru.myproevent.databinding.FragmentLoginBinding
import ru.myproevent.ui.BackButtonListener
import ru.myproevent.ui.fragments.BaseMvpFragment
import ru.myproevent.ui.presenters.authorization.login.LoginPresenter
import ru.myproevent.ui.presenters.authorization.login.LoginView
import ru.myproevent.ui.presenters.main.RouterProvider

class LoginFragment : BaseMvpFragment(), LoginView, BackButtonListener {
    private var _view: FragmentLoginBinding? = null
    private val view get() = _view!!

    override val presenter by moxyPresenter {
        LoginPresenter((parentFragment as RouterProvider).router).apply {
            ProEventApp.instance.appComponent.inject(this)
        }
    }

    companion object {
        fun newInstance() = LoginFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _view = FragmentLoginBinding.inflate(inflater, container, false).apply {
            confirmLogin.setOnClickListener { presenter.confirmLogin(loginEdit.text.toString()) }
        }
        return view.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _view = null
    }
}