package de.tebbeubben.remora.ui.commands

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import de.tebbeubben.remora.R
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.time.Clock
import kotlin.time.Instant

@Composable
fun DateTimePicker(
    selection: MutableState<Instant?>,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val localDateTime = selection.value?.toLocalDateTime(TimeZone.currentSystemDefault())?.toJavaLocalDateTime()

    val formattedDate = localDateTime?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)) ?: "Today"
    val formattedTime = localDateTime?.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)) ?: "Now"

    val colorVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val color = if (selection.value == null) colorVariant else Color.Unspecified

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        TextButton(
            colors = ButtonDefaults.textButtonColors(contentColor = color),
            onClick = { showDatePicker = true },
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(R.drawable.date_range_24px),
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text(formattedDate)
            }
        }

        TextButton(
            colors = ButtonDefaults.textButtonColors(contentColor = color),
            onClick = { showTimePicker = true },
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(R.drawable.schedule_24px),
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text(formattedTime)
            }
        }
    }


    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDate = (selection.value ?: Clock.System.now()).toLocalDateTime(TimeZone.currentSystemDefault()).date.toJavaLocalDate()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val timezone = TimeZone.currentSystemDefault()
                        val time = (selection.value ?: Clock.System.now()).toLocalDateTime(timezone).time
                        val date = Instant.fromEpochMilliseconds(it).toLocalDateTime(timezone).date
                        val dateTime = LocalDateTime(date, time)
                        selection.value = dateTime.toInstant(timezone)
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val localTime = (selection.value ?: Clock.System.now()).toLocalDateTime(TimeZone.currentSystemDefault()).time
        val timePickerState = rememberTimePickerState(
            initialHour = localTime.hour,
            initialMinute = localTime.minute,
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Dismiss")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val timezone = TimeZone.currentSystemDefault()
                    val date = (selection.value ?: Clock.System.now()).toLocalDateTime(timezone).date
                    val time = LocalTime(timePickerState.hour, timePickerState.minute)
                    val dateTime = LocalDateTime(date, time)
                    selection.value = dateTime.toInstant(timezone)
                    showTimePicker = false
                }) {

                    Text("OK")
                }
            },
            text = {
                TimePicker(
                    state = timePickerState
                )
            }
        )
    }
}