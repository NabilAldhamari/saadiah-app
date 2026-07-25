package app.saadiah.ui

import android.app.AlarmManager
import android.content.Context
import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import app.saadiah.alarm.canPostNotifications
import app.saadiah.design.MinimumTapTarget
import app.saadiah.design.SaadiahSpacing
import app.saadiah.design.SaadiahType
import app.saadiah.doctor.DeliveryLog
import app.saadiah.doctor.DeliveryRecord
import app.saadiah.doctor.isKnownRestrictive

@Composable
fun DoctorScreen(
    onOpenSettings: () -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val records = remember { DeliveryLog(context).recent() }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier =
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = SaadiahSpacing.screen, vertical = SaadiahSpacing.large),
        ) {
            ScreenHeader(title = "Will my alerts arrive?", onBack = onBack)
            Checks(context)
            Spacer(Modifier.height(SaadiahSpacing.large))
            Guidance(onOpenSettings = onOpenSettings)
            Spacer(Modifier.height(SaadiahSpacing.large))
            DeliveryHistory(records)
        }
    }
}

@Composable
private fun Checks(context: Context) {
    CheckRow(label = "Notifications allowed", passing = canPostNotifications(context))
    CheckRow(label = "Exact alarms allowed", passing = canScheduleExactAlarms(context))
}

@Composable
private fun CheckRow(
    label: String,
    passing: Boolean,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = MinimumTapTarget)
                .padding(vertical = SaadiahSpacing.medium / 2),
    ) {
        Body(if (passing) "$label — yes" else "$label — no")
        HorizontalDivider()
    }
}

@Composable
private fun Guidance(onOpenSettings: () -> Unit) {
    val restrictive = isKnownRestrictive(Build.MANUFACTURER)
    Caption(
        if (restrictive) {
            "${Build.MANUFACTURER} phones stop background apps by default, which can hold prayer " +
                "alerts back. Open the settings screen and allow Saadiah to run."
        } else {
            "If an alert ever arrives late, allow Saadiah to run in the background."
        },
    )
    TextButton(onClick = onOpenSettings, modifier = Modifier.heightIn(min = MinimumTapTarget)) {
        Text(text = "Open background settings", fontSize = SaadiahType.body.size)
    }
}

@Composable
private fun DeliveryHistory(records: List<DeliveryRecord>) {
    Caption("Recent alerts")
    Spacer(Modifier.height(SaadiahSpacing.medium / 2))
    if (records.isEmpty()) {
        Body("No alert has arrived yet. Once one does, its timing is recorded here.")
        return
    }
    for (record in records) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = MinimumTapTarget)
                    .padding(vertical = SaadiahSpacing.medium / 2),
        ) {
            Body("${record.prayerName} — ${record.describeDelay()}")
            HorizontalDivider()
        }
    }
}

private fun canScheduleExactAlarms(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    return alarmManager.canScheduleExactAlarms()
}
