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
        Column(modifier = Modifier.fillMaxSize()) {
            ScreenHeader(title = strings.alertsArriveQuestion, onBack = onBack)
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = SaadiahSpacing.screen, vertical = SaadiahSpacing.large),
            ) {
                Checks(context)
                Spacer(Modifier.height(SaadiahSpacing.large))
                Guidance(onOpenSettings = onOpenSettings)
                Spacer(Modifier.height(SaadiahSpacing.large))
                DeliveryHistory(records)
            }
        }
    }
}

@Composable
private fun Checks(context: Context) {
    CheckRow(label = strings.notificationsAllowed, passing = canPostNotifications(context))
    CheckRow(label = strings.exactAlarmsAllowed, passing = canScheduleExactAlarms(context))
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
        Body(if (passing) "$label — ${strings.yes}" else "$label — ${strings.no}")
        HorizontalDivider()
    }
}

@Composable
private fun Guidance(onOpenSettings: () -> Unit) {
    val restrictive = isKnownRestrictive(Build.MANUFACTURER)
    Caption(
        if (restrictive) strings.backgroundAdviceRestrictive(Build.MANUFACTURER) else strings.backgroundAdviceGeneric,
    )
    TextButton(onClick = onOpenSettings, modifier = Modifier.heightIn(min = MinimumTapTarget)) {
        Text(text = strings.openBackgroundSettings, fontSize = SaadiahType.body.size)
    }
}

@Composable
private fun DeliveryHistory(records: List<DeliveryRecord>) {
    Caption(strings.recentAlerts)
    Spacer(Modifier.height(SaadiahSpacing.medium / 2))
    if (records.isEmpty()) {
        Body(strings.noAlertYet)
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
            Body("${record.prayerName} — ${record.describeDelay(strings)}")
            HorizontalDivider()
        }
    }
}

private fun canScheduleExactAlarms(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    return alarmManager.canScheduleExactAlarms()
}
