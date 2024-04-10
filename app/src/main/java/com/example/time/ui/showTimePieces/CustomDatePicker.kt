package com.example.time.ui.showTimePieces

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.time.ui.TimeViewModel
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.models.CalendarConfig
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
import com.maxkeppeler.sheets.calendar.models.CalendarStyle
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DatePeriodPicker(viewModel: TimeViewModel) {
    val dateFrom = remember { mutableStateOf(LocalDate.now()) }
    val dateTo = remember { mutableStateOf(LocalDate.now().plusDays(1)) }

    // 计算当前时间与 1970 年 1 月 1 日午夜之间的时间差（毫秒数）
    val msTimeFrom = remember {
        mutableStateOf(
            dateFrom.value.atStartOfDay(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli()
        )
    }
    val msTimeTo = remember {
        mutableStateOf(
            dateTo.value.atStartOfDay(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli()
        )
    }
    viewModel.getTimePiecesBetween(msTimeFrom.value, msTimeTo.value)

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            CustomDatePicker(
                value = dateFrom.value,
                onValueChange = {
                    msTimeFrom.value =
                        it.atStartOfDay(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli()
                    dateFrom.value = it
                },
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Arrow",
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            CustomDatePicker(
                value = dateTo.value,
                onValueChange = {
                    msTimeTo.value =
                        it.atStartOfDay(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli()
                    dateTo.value = it
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDatePicker(
    value: LocalDate,
    onValueChange: (LocalDate) -> Unit,
    modifier: Modifier
) {

    val open = remember { mutableStateOf(false) }

    if (open.value) {
        CalendarDialog(
            state = rememberUseCaseState(
                visible = true,
                true,
                onCloseRequest = { open.value = false }),
            config = CalendarConfig(
                yearSelection = true,
                style = CalendarStyle.MONTH,
            ),
            selection = CalendarSelection.Date(
                selectedDate = value
            ) { newDate ->
                onValueChange(newDate)
            },
        )
    }

    TextField(
        modifier = modifier.clickable { //Click event
            open.value = true
        },
        enabled = false,// <- Add this to make click event work
        value = value.format(DateTimeFormatter.ISO_DATE),
        onValueChange = {},
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    )
}