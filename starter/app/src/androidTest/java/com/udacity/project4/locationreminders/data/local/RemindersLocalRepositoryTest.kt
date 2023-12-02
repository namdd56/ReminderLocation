package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    //    TODO: Add testing implementation to the RemindersLocalRepository.kt
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: RemindersLocalRepository
    private lateinit var remindersDatabase: RemindersDatabase

    @Before
    fun init() {
        remindersDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
        repository = RemindersLocalRepository(remindersDatabase.reminderDao(), Dispatchers.Main)
    }

    @After
    fun closeDb() = remindersDatabase.close()

    @Test
    fun test_getReminder_returnSuccess() = runTest {
        var reminderData = ReminderDTO("title1", "description1", "location1", 23.45, 45.55, "1")
        repository.saveReminder(reminderData)
        val result = repository.getReminder("1")
        result as Result.Success
        assertThat(result, `is`(true))
    }
    @Test
    fun test_getReminder_returnError() = runTest {
        var reminderData = ReminderDTO("title1", "description1", "location1", 23.45, 45.55, "1")
        repository.saveReminder(reminderData)
        val result = repository.getReminder("2")
        result as Result.Success
        assertThat(result, `is`(false))
    }
}

