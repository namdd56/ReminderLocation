package com.udacity.project4

import android.app.Activity
import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.hamcrest.core.Is.`is`
import org.hamcrest.core.IsNot.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.get
import org.koin.test.junit5.AutoCloseKoinTest

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    //    TODO: add End to End testing to the app
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Test
    fun test_onClickAddButton() = runTest {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle)).check(matches(isDisplayed()))
        onView(withId(R.id.reminderDescription)).check(matches(isDisplayed()))
        onView(withId(R.id.selectLocation)).check(matches(isDisplayed()))
        activityScenario.close()
    }

    @Test
    fun test_addReminder() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
        val title = "title1"
        val description = "description1"

        onView(withText(R.string.no_data)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle)).perform(ViewActions.typeText(title))
        onView(withId(R.id.reminderDescription)).perform(ViewActions.typeText(description))
        onView(withId(R.id.selectLocation)).perform(click())
        onView(withId(R.id.map_fragment)).perform(click())
        runBlocking {
            delay(2000)
        }
        onView(withId(R.id.saveButton)).perform(click())
        onView(withId(R.id.saveReminder)).perform(click())
        onView(withText(title)).check(matches(isDisplayed()))
        onView(withText(description)).check(matches(isDisplayed()))

        onView(withText(title)).perform(click())
        onView(withText(title)).check(matches(isDisplayed()))
        onView(withText(description)).check(matches(isDisplayed()))

        activityScenario.close()


    }

    @Test
    fun test_showsSnackbar() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
        val title = "title1"
        val description = "description1"
        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle)).perform(ViewActions.typeText(title))
        onView(withId(R.id.reminderDescription)).perform(ViewActions.typeText(description))
        closeSoftKeyboard()
        onView(withId(R.id.saveReminder)).perform(click())
        runBlocking {
            delay(2000)
        }
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.err_select_location)))
        activityScenario.close()
    }

    private fun getActivity(activityScenario: ActivityScenario<RemindersActivity>) : Activity? {
        var activity: Activity? = null
        activityScenario.onActivity {
            activity = it
        }
        return activity
    }
    @Test
    fun test_showsToat() {
        // please test this case with API 29
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
        val activity = getActivity(activityScenario)

        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.selectLocation)).perform(click())

        onView(withId(R.id.saveButton)).perform(click())
        onView(withText(R.string.select_location)).inRoot(
            withDecorView(
                not(
                    `is`(
                        activity!!.window.decorView
                    )
                )
            )
        ).check(matches(isDisplayed()))
        runBlocking {
            delay(2000)
        }
        activityScenario.close()
    }


}
