package com.remotearthsolutions.expensetracker.fragments.settings

import android.content.Context
import android.content.Intent
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.remotearthsolutions.expensetracker.R
import com.remotearthsolutions.expensetracker.activities.main.MainActivity
import com.remotearthsolutions.expensetracker.utils.AnalyticsManager
import com.remotearthsolutions.expensetracker.utils.LocalNotificationManager
import com.remotearthsolutions.expensetracker.utils.Utils
import kotlin.random.Random

class AddExpenseReminderWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : Worker(appContext, workerParams) {
    override fun doWork(): Result {
        if (isStopped)
            return Result.failure()

        val messages = appContext.resources.getStringArray(R.array.daily_reminder_messages)
        val message = messages[Random.nextInt(messages.size)]

        val intent = Intent(appContext, MainActivity::class.java)
        intent.putExtra("message", message)

        val count = ReminderWorkerHelper.getCountOfExpenseAddedInLastFourHoursSync(appContext)
        if (count == 0) {
            with(AnalyticsManager) { logEvent(REMINDED_TO_ADD_EXPENSE) }
            LocalNotificationManager.showNotification(
                appContext,
                appContext.getString(R.string.app_name),
                message,
                Utils.getPendingIntent(appContext, intent, 0)
            )
        }

        ReminderWorkerHelper.setAddExpenseReminder(appContext)
        return Result.success()
    }
}