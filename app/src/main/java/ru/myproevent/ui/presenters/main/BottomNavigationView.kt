package ru.myproevent.ui.presenters.main

import moxy.viewstate.strategy.alias.AddToEnd
import moxy.viewstate.strategy.alias.OneExecution
import ru.myproevent.ui.presenters.BaseMvpView

/**
 * Функции этого интерфейса должны вызываться исключительно из методов BottomNavigationPresenter.
 * Для реализации этой инкапсуляции в качесве параметра передаётся BottomNavigationPresenterFriendAccess,
 * экземпляр которого может иметь только BottomNavigationPresenter
 */
@AddToEnd
interface BottomNavigationView : BaseMvpView {
    fun showTab(
        tab: Tab,
        friendAccess: BottomNavigationPresenter.BottomNavigationPresenterFriendAccess
    )

    @OneExecution
    fun resetState(friendAccess: BottomNavigationPresenter.BottomNavigationPresenterFriendAccess)
}