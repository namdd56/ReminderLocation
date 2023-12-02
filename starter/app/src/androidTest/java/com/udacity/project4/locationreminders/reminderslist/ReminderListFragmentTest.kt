package com.udacity.project4.locationreminders.reminderslist

import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {

    //    TODO: test the navigation of the fragments.
    //    TODO: test the displayed data on the UI.
    //    TODO: add testing for the error messages.
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var reminderDataSource: ReminderDataSource
    private val dataBindingIdlingResource = DataBindingIdlingResource()
    private val mockNavController = mock(NavController::class.java)

    @Before
    fun init() {
        stopKoin()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    getApplicationContext(),
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    getApplicationContext(),
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(getApplicationContext()) }
        }
        startKoin {
            modules(listOf(myModule))
        }
        reminderDataSource = GlobalContext.get().get()
        runBlocking {
            reminderDataSource.deleteAllReminders()
        }
    }

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Test
    fun test_navigation() {
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, mockNavController)
        }
        onView(withId(R.id.addReminderFAB)).perform(click())
        verify(mockNavController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }

    @Test
    fun test_errormessage() {
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)
        onView(withText(R.string.no_data)).check(matches(isDisplayed()))
    }

    @Test
    fun test_displayData() = runTest {
        var reminderData = ReminderDTO("title1", "description1", "location1", 23.45, 45.55, "1")
        reminderDataSource.saveReminder(reminderData)
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)
        onView(withId(R.id.noDataTextView)).check(matches(not(isDisplayed())))
        onView(withText(reminderData.title)).check(matches(isDisplayed()))
        onView(withText(reminderData.description)).check(matches(isDisplayed()))
        onView(withText(reminderData.location)).check(matches(isDisplayed()))

    }

}