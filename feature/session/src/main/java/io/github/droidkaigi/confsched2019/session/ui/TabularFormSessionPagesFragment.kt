package io.github.droidkaigi.confsched2019.session.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import com.google.android.material.tabs.TabLayout
import com.soywiz.klock.DateTime
import com.soywiz.klock.hours
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import io.github.droidkaigi.confsched2019.di.PageScope
import io.github.droidkaigi.confsched2019.model.SessionPage
import io.github.droidkaigi.confsched2019.session.R
import io.github.droidkaigi.confsched2019.session.ui.widget.DaggerFragment
import io.github.droidkaigi.confsched2019.session.databinding.FragmentTabularFormSessionPagesBinding
import io.github.droidkaigi.confsched2019.session.di.SessionPageScope
import io.github.droidkaigi.confsched2019.session.ui.store.SessionContentsStore
import javax.inject.Inject

class TabularFormSessionPagesFragment : DaggerFragment() {

    private lateinit var binding: FragmentTabularFormSessionPagesBinding
    @Inject lateinit var sessionContentsStore: SessionContentsStore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_tabular_form_session_pages,
            container,
            false
        )
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupSessionPages()
    }

    private fun setupSessionPages() {
        val days = listOf(SessionPage.pageOfDay(1), SessionPage.pageOfDay(2))

        // For a few reasons, we stopped using ViewPager in this fragment.
        // See https://github.com/DroidKaigi/conference-app-2019/issues/622 .
        binding.tabularFormSessionsTabLayout.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    val day = days[tab.position].day
                    val tag = createTabularFormSessionPageFragmentTag(day)
                    val fragment = childFragmentManager.findFragmentByTag(tag)
                    if (fragment == null) {
                        val args = TabularFormSessionPagesFragmentArgs.Builder()
                            .setDay(day)
                            .build()
                        val selectedFragment = TabularFormSessionPageFragment.newInstance(args)
                        childFragmentManager.beginTransaction()
                            .replace(R.id.tabular_form_sessions_fragment_container, selectedFragment, tag)
                            .commit()
                    }
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {}

                override fun onTabUnselected(tab: TabLayout.Tab?) {}
            })

        days.forEachIndexed { index, day ->
            val newTab = binding.tabularFormSessionsTabLayout.newTab().apply {
                text = day.title
            }
            binding.tabularFormSessionsTabLayout.addTab(newTab)

            // Select first tab
            if (index == 0) {
                newTab.select()
            }

            // Select second tab
            if (index == 1) {
                val jstNow = DateTime.now().toOffset(9.hours)
                if (jstNow.yearInt == 2019 && jstNow.month1 == 2 && jstNow.dayOfMonth == 8) {
                    newTab.select()
                }
            }
        }
    }

    private fun createTabularFormSessionPageFragmentTag(day: Int) = "TabularFormSessionPageFragment$day"
}

@Module
abstract class TabularFromSessionPagesFragmentModule {

    @SessionPageScope
    @ContributesAndroidInjector(modules = [TabularFormSessionPageFragmentModule::class])
    abstract fun contributeTabularFormSessionPageFragment(): TabularFormSessionPageFragment

    @Module
    companion object {
        @JvmStatic @Provides
        @PageScope
        fun providesLifecycle(
            tabularFromSessionPagesFragment: TabularFormSessionPagesFragment
        ): Lifecycle {
            return tabularFromSessionPagesFragment.viewLifecycleOwner.lifecycle
        }
    }
}
