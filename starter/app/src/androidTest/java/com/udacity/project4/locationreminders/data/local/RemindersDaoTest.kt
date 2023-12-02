package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    //    TODO: Add testing implementation to the RemindersDao.kt
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()
    private lateinit var remindersDatabase: RemindersDatabase

    @Before
    fun init() {
        remindersDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() = remindersDatabase.close()

    @Test
    fun test_getReminderByID() = runTest {
        var reminderData = ReminderDTO("title1", "description1", "location1", 23.45, 45.55, "1")
        remindersDatabase.reminderDao().saveReminder(reminderData)

        val loadedReminder = remindersDatabase.reminderDao().getReminderById(reminderData.id)

        assertThat(loadedReminder as ReminderDTO, notNullValue())
        assertThat(loadedReminder.id, `is`(reminderData.id))
        assertThat(loadedReminder.title, `is`(reminderData.title))
        assertThat(loadedReminder.description, `is`(reminderData.description))
        assertThat(loadedReminder.location, `is`(reminderData.location))
        assertThat(loadedReminder.latitude, `is`(reminderData.latitude))
        assertThat(loadedReminder.longitude, `is`(reminderData.longitude))
    }

    @Test
    fun test_getAllReminder() = runTest {
        val reminder1 = ReminderDTO("title 1", "description 1", "location 1", 20.55, 30.552)
        val reminder2 = ReminderDTO("title 2", "description 2", "location 2", 27.02, 9.3)
        val reminder3 = ReminderDTO("title 3", "description 3", "location 3", 37.2, 3.2)
        remindersDatabase.reminderDao().saveReminder(reminder1)
        remindersDatabase.reminderDao().saveReminder(reminder2)
        remindersDatabase.reminderDao().saveReminder(reminder3)

        val loadedReminders = remindersDatabase.reminderDao().getReminders()

        assertThat(loadedReminders.size, `is`(3))
        assertThat(loadedReminders[0].id, `is`(reminder1.id))
        assertThat(loadedReminders[1].id, `is`(reminder2.id))
        assertThat(loadedReminders[2].id, `is`(reminder3.id))
    }

}