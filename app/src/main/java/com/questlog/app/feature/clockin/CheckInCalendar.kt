package com.questlog.app.feature.clockin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.questlog.app.R
import com.questlog.app.ui.designsystem.QuestlogSoftBrown
import com.questlog.app.ui.designsystem.QuestlogSpacing
import com.questlog.app.ui.designsystem.QuestlogTerracotta
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch

private const val HISTORY_MONTHS = 12L
private val MONTH_TITLE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

@Composable
fun CheckInCalendar(
    checkInDays: Set<Long>,
    modifier: Modifier = Modifier,
) {
    val today = rememberToday()
    val currentMonth = YearMonth.from(today)
    val state = rememberCalendarState(
        startMonth = currentMonth.minusMonths(HISTORY_MONTHS),
        endMonth = currentMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = daysOfWeek().first(),
    )
    val scope = rememberCoroutineScope()

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.calendar_title),
            style = MaterialTheme.typography.titleMedium,
        )

        Spacer(modifier = Modifier.height(QuestlogSpacing.M))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = MONTH_TITLE_FORMAT.format(state.firstVisibleMonth.yearMonth),
                style = MaterialTheme.typography.titleSmall,
                color = QuestlogSoftBrown,
                modifier = Modifier.weight(1f),
            )
            TodayChip(
                onClick = { scope.launch { state.animateScrollToMonth(currentMonth) } },
            )
        }

        Spacer(modifier = Modifier.height(QuestlogSpacing.S))

        WeekdayHeader(firstDayOfWeek = state.firstDayOfWeek)

        HorizontalCalendar(
            state = state,
            dayContent = { day ->
                DayCell(day = day, checkInDays = checkInDays, today = today)
            },
        )
    }
}

@Composable
private fun TodayChip(onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    Box(
        modifier = Modifier
            .clip(shape)
            .border(1.dp, QuestlogTerracotta, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = QuestlogSpacing.M, vertical = QuestlogSpacing.Xs),
    ) {
        Text(
            text = stringResource(R.string.calendar_today),
            style = MaterialTheme.typography.labelMedium,
            color = QuestlogTerracotta,
        )
    }
}

@Composable
private fun WeekdayHeader(firstDayOfWeek: java.time.DayOfWeek) {
    Row(modifier = Modifier.fillMaxWidth()) {
        daysOfWeek(firstDayOfWeek).forEach { dayOfWeek ->
            Text(
                text = dayOfWeek.getDisplayName(
                    java.time.format.TextStyle.NARROW,
                    Locale.getDefault(),
                ),
                style = MaterialTheme.typography.labelMedium,
                color = QuestlogSoftBrown,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun DayCell(
    day: CalendarDay,
    checkInDays: Set<Long>,
    today: LocalDate,
) {
    if (day.position != DayPosition.MonthDate) {
        Box(modifier = Modifier.size(DAY_SIZE))
        return
    }

    val isToday = day.date == today
    val isFuture = day.date.isAfter(today)
    val isCheckedIn = day.date.toEpochDay() in checkInDays

    Box(
        modifier = Modifier
            .size(DAY_SIZE)
            .padding(3.dp),
        contentAlignment = Alignment.Center,
    ) {
        when {
            isToday && isCheckedIn -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(QuestlogTerracotta),
                contentAlignment = Alignment.Center,
            ) {
                DayNumber(day = day, color = Color.White)
            }

            isToday -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .border(1.5.dp, QuestlogTerracotta, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                DayNumber(day = day, color = QuestlogTerracotta)
            }

            else -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                DayNumber(
                    day = day,
                    color = if (isFuture) {
                        QuestlogSoftBrown.copy(alpha = 0.38f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
                Spacer(modifier = Modifier.height(2.dp))
                if (isCheckedIn && !isFuture) {
                    Box(
                        modifier = Modifier
                            .size(DOT_SIZE)
                            .clip(CircleShape)
                            .background(QuestlogTerracotta),
                    )
                } else {
                    Spacer(modifier = Modifier.size(DOT_SIZE))
                }
            }
        }
    }
}

@Composable
private fun DayNumber(day: CalendarDay, color: Color) {
    Text(
        text = day.dayOfMonth.toString(),
        style = MaterialTheme.typography.bodyMedium,
        color = color,
    )
}

private val CalendarDay.dayOfMonth: Int
    get() = date.dayOfMonth

private val DAY_SIZE = 40.dp
private val DOT_SIZE = 6.dp

@Composable
private fun rememberToday(): LocalDate = remember { LocalDate.now() }
