package ru.myproevent.ui.activity

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.github.terrakok.cicerone.Navigator
import com.github.terrakok.cicerone.NavigatorHolder
import com.github.terrakok.cicerone.Router
import com.github.terrakok.cicerone.androidx.AppNavigator
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import ru.myproevent.ProEventApp
import ru.myproevent.R
import ru.myproevent.databinding.ActivityMainBinding
import ru.myproevent.ui.BackButtonListener
import ru.myproevent.ui.fragments.TabContainerFragment
import ru.myproevent.ui.presenters.main.BottomNavigationPresenter
import ru.myproevent.ui.presenters.main.BottomNavigationView
import ru.myproevent.ui.presenters.main.Tab
import javax.inject.Inject

class BottomNavigationActivity : MvpAppCompatActivity(), BottomNavigationView {
    private val navigator: Navigator = AppNavigator(this, R.id.container)

    @Inject
    lateinit var navigatorHolder: NavigatorHolder

    private var _view: ActivityMainBinding? = null
    private val view get() = _view!!

    @Inject
    lateinit var router: Router

    @InjectPresenter
    lateinit var presenter: BottomNavigationPresenter

    @ProvidePresenter
    fun createBottomNavigationPresenter() =
        BottomNavigationPresenter(router).apply { ProEventApp.instance.appComponent.inject(this) }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ProEventApp.instance.appComponent.inject(this)
        setTheme(R.style.Theme_Proevent_NoActionBar)
        super.onCreate(savedInstanceState)
        _view = ActivityMainBinding.inflate(layoutInflater)
        setContentView(view.root)
        initViews()
    }

    override fun checkTab(tab: Tab) {
        when (tab) {
            Tab.HOME -> R.id.home_tab
            Tab.CONTACTS -> R.id.contacts_tab
            Tab.CHAT -> R.id.chat_tab
            Tab.EVENTS -> R.id.events_tab
            Tab.SETTINGS -> R.id.settings_tab
            else -> null
        }?.let { view.bottomNavigationBar.menu.findItem(it).isChecked = true }
    }

    override fun hideBottomNavigation() {
        if (view.bottomNavigationBar.visibility == View.GONE) {
            return
        }
        view.bottomNavigationBar.visibility = View.GONE
    }

    override fun showBottomNavigation() {
        if (view.bottomNavigationBar.visibility == View.VISIBLE) {
            return
        }
        view.bottomNavigationBar.visibility = View.VISIBLE
    }

    private fun initViews() {
        with(view) {
            view.bottomNavigationBar.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.home_tab -> openTab(Tab.HOME)
                    R.id.contacts_tab -> openTab(Tab.CONTACTS)
                    R.id.chat_tab -> openTab(Tab.CHAT)
                    R.id.events_tab -> openTab(Tab.EVENTS)
                    R.id.settings_tab -> openTab(Tab.SETTINGS)
                }
                true
            }
        }
    }

    override fun openTab(tab: Tab) {
        val fm = supportFragmentManager
        var currentFragment: Fragment? = null
        val fragments = fm.fragments
        for (f in fragments) {
            if (f.isVisible) {
                currentFragment = f
                break
            }
        }
        val newFragment = fm.findFragmentByTag(tab.name)
        if (currentFragment != null && newFragment != null && currentFragment === newFragment) return
        val transaction = fm.beginTransaction()
        transaction.setCustomAnimations(
            R.anim.fade_in,
            R.anim.fade_out,
            R.anim.fade_in,
            R.anim.fade_out
        )
        if (newFragment == null) {
            val tabContainer = when (tab) {
                Tab.AUTHORIZATION -> TabContainerFragment.newInstance(Tab.AUTHORIZATION)
                Tab.HOME -> TabContainerFragment.newInstance(Tab.HOME)
                Tab.CONTACTS -> TabContainerFragment.newInstance(Tab.CONTACTS)
                Tab.CHAT -> TabContainerFragment.newInstance(Tab.CHAT)
                Tab.EVENTS -> TabContainerFragment.newInstance(Tab.EVENTS)
                Tab.SETTINGS -> TabContainerFragment.newInstance(Tab.SETTINGS)
            }
            transaction.add(
                R.id.container,
                tabContainer, tab.name
            )
        }
        if (currentFragment != null) {
            transaction.hide(currentFragment)
        }
        if (newFragment != null) {
            transaction.show(newFragment)
        }
        transaction.commitNow()
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        navigatorHolder.setNavigator(navigator)
    }

    override fun onPause() {
        navigatorHolder.removeNavigator()
        super.onPause()
    }

    override fun onBackPressed() {
        val fm = supportFragmentManager
        var fragment: Fragment? = null
        val fragments = fm.fragments
        for (f in fragments) {
            if (f.isVisible) {
                fragment = f
                break
            }
        }
        if (fragment != null && fragment is BackButtonListener
            && (fragment as BackButtonListener).onBackPressed()
        ) {
            return
        } else {
            presenter.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _view = null
    }
}