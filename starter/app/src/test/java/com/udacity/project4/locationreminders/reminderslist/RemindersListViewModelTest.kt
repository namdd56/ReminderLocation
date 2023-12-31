package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(maxSdk = Build.VERSION_CODES.P)
class RemindersListViewModelTest {

    //TODO: provide testing to the RemindersListViewModel and its live data objects
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var remindersListViewModel: RemindersListViewModel

    @Before
    fun setUp() {
        stopKoin()
        fakeDataSource = FakeDataSource()
        remindersListViewModel =
            RemindersListViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

//    @Test
//    fun test_returnError() = mainCoroutineRule.runBlockingTest {
//        fakeDataSource.setReturnError(true)
//        val reminderData = ReminderDTO("title1", "description1", "location1", 23.45, 45.55, "1")
//        Dispatchers.setMain(StandardTestDispatcher())
//        fakeDataSource.saveReminder(reminderData)
//        remindersListViewModel.loadReminders()
//        assertThat(remindersListViewModel.showSnackBar.value, `is`("Reminders not found"))
//    }
//


    @Test
    fun test_checkLoading() = runTest(UnconfinedTestDispatcher()) {
        val reminderData = ReminderDTO("title1", "description1", "location1", 23.45, 45.55, "1")
        Dispatchers.setMain(StandardTestDispatcher())
        fakeDataSource.saveReminder(reminderData)
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(true))
        advanceUntilIdle()
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(false)
        )
    }

    @Test
    fun test_deleteAllReminder() = runTest(UnconfinedTestDispatcher()) {
        remindersListViewModel.loadReminders()
        fakeDataSource.deleteAllReminders()
        assertThat(remindersListViewModel.showNoData.value, `is`(true))
    }

    @Test
    fun shouldReturnError() = runTest(UnconfinedTestDispatcher()) {
        fakeDataSource.setReturnError(true)
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showSnackBar.getOrAwaitValue(), `is`("ERROR")
        )
    }
}