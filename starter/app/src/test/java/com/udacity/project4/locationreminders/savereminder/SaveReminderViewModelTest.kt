package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.CoreMatchers.`is`
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(maxSdk = Build.VERSION_CODES.P)
class SaveReminderViewModelTest {


    //TODO: provide testing to the SaveReminderView and its live data objects
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var application: Application

    @Before
    fun setUp() {
        stopKoin()
        fakeDataSource = FakeDataSource()
        application = ApplicationProvider.getApplicationContext()
        saveReminderViewModel =
            SaveReminderViewModel(application, fakeDataSource)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun test_validateReminders() = mainCoroutineRule.runBlockingTest {
        //GIVEN
        val reminderData = ReminderDataItem("title1", "description1", "location1", 23.45, 45.55, "1")
        mainCoroutineRule.pauseDispatcher()
        saveReminderViewModel.validateAndSaveReminder(reminderData)
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(true))
        mainCoroutineRule.resumeDispatcher()
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun test_saveReminder() {
        val reminderData = ReminderDataItem("title1", "description1", "location1", 23.45, 45.55, "1")
        saveReminderViewModel.saveReminder(reminderData)

        assertThat(saveReminderViewModel.showToast.getOrAwaitValue(), `is`(application.getString(R.string.reminder_saved)))
        Assert.assertEquals(saveReminderViewModel.navigationCommand.getOrAwaitValue(), NavigationCommand.Back)
    }


}