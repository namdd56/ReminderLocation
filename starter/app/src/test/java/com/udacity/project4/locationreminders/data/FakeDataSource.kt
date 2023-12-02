package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
@Suppress("UNREACHABLE_CODE")
class FakeDataSource : ReminderDataSource {

    //    TODO: Create a fake data source to act as a double to the real data source
    private var reminderDTOList = mutableListOf<ReminderDTO>()
    private var returnError: Boolean = false

    fun setReturnError(error: Boolean) {
        returnError = error
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        TODO("Return the reminders")
        if (returnError) return Result.Error("ERROR")
        else {
            reminderDTOList.let { return Result.Success(ArrayList(it)) }
            return Result.Error(
                "Reminders not found"
            )
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        TODO("save the reminder")
        reminderDTOList.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        TODO("return the reminder with the id")
        return when {
            returnError -> Result.Error("error")
            else -> when (val reminder = reminderDTOList.find { it.id == id }) {
                null -> {
                    Result.Error("Not Found")
                }

                else -> {
                    Result.Success(reminder)
                }
            }
        }
    }

    override suspend fun deleteAllReminders() {
        TODO("delete all the reminders")
        reminderDTOList.clear()
    }


}