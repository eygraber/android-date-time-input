package com.eygraber.date_time_input

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import com.eygraber.date_time_input.common.DateResult
import com.eygraber.date_time_input.common.TimeResult
import com.eygraber.date_time_input.compose.DateInput
import com.eygraber.date_time_input.compose.DateInputHolder
import com.eygraber.date_time_input.compose.DateInputStyle
import com.eygraber.date_time_input.xml.DateInputView
import com.eygraber.date_time_input.xml.TimeInputView
import com.google.android.material.composethemeadapter.MdcTheme
import java.time.LocalDate
import java.time.LocalTime
import java.time.Month
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class MainActivity : AppCompatActivity() {
  private val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
  private val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM)

  private val minDate = LocalDate.of(1950, Month.JANUARY, 1)
  private val maxDate = LocalDate.of(2050, Month.JANUARY, 1)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    configureDateInputView(R.id.date_input_view)

    configureTimeInputView(R.id.time_input_view)

    configureDateInputView(R.id.outlined_date_input_view)

    configureTimeInputView(R.id.outlined_time_input_view)

    findViewById<ComposeView>(R.id.date_input_composable).setContent {
      MdcTheme {
        Column {
          MyDateInput(style = DateInputStyle.Filled)

          MyDateInput(style = DateInputStyle.Outlined, modifier = Modifier.padding(top = 16.dp))
        }
      }
    }
  }

  private fun configureDateInputView(@IdRes id: Int) {
    findViewById<DateInputView>(id).let { view ->
      view.selectedDate = LocalDate.now()
      view.minDate = minDate
      view.maxDate = maxDate

      view.addOnDateChangedListener { dateResult ->
        view.error = dateResult.calculateError(dateFormatter)
      }
    }
  }

  private fun configureTimeInputView(@IdRes id: Int) {
    findViewById<TimeInputView>(id).let { view ->
      view.selectedTime = LocalTime.now()

      view.addOnTimeChangedListener { timeResult ->
        view.error = timeResult.calculateError(timeFormatter)
      }
    }
  }

  @Composable
  private fun MyDateInput(
    style: DateInputStyle,
    modifier: Modifier = Modifier
  ) {
    var error by remember { mutableStateOf<String?>(null) }

    DateInput(
      dateInputHolder = DateInputHolder(
        initialDate = LocalDate.now(),
        minDate = minDate,
        maxDate = maxDate
      ),
      error = error?.let {
        {
          Text(
            text = it,
            modifier = Modifier.padding(top = 16.dp, start = 16.dp),
            color = MaterialTheme.colors.error,
            style = MaterialTheme.typography.caption
          )
        }
      },
      modifier = modifier,
      style = style
    ) { dateResult ->
      error = dateResult.calculateError(dateFormatter)
    }
  }
}

private fun DateResult.calculateError(formatter: DateTimeFormatter) = when(this) {
  is DateResult.Success -> null
  is DateResult.Error -> error.message

  is DateResult.ViolatedMaxDate ->
    "${date.format(formatter)} needs to be less than ${maxDate.format(formatter)}"
  is DateResult.ViolatedMinDate -> when {
    date.year < 1_000 -> "Date needs to be greater than ${minDate.format(formatter)}"
    else -> "${date.format(formatter)} needs to be greater than ${minDate.format(formatter)}"
  }

  DateResult.RequiresMonth -> null
  DateResult.RequiresDay -> null
  DateResult.RequiresYear -> null
}

private fun TimeResult.calculateError(formatter: DateTimeFormatter) = when(this) {
  is TimeResult.Success -> null
  is TimeResult.Error -> error.message

  is TimeResult.ViolatedMaxTime ->
    "${time.format(formatter)} needs to be less than ${maxTime.format(formatter)}"
  is TimeResult.ViolatedMinTime ->
    "${time.format(formatter)} needs to be greater than ${minTime.format(formatter)}"

  TimeResult.RequiresHour -> null
  TimeResult.RequiresMinute -> null
  TimeResult.RequiresSecond -> null
}
