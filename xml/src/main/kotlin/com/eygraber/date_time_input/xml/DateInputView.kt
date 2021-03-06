package com.eygraber.date_time_input.xml

import android.content.Context
import android.text.InputFilter
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.constraintlayout.helper.widget.Flow
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.widget.doAfterTextChanged
import com.eygraber.date_time_input.common.DateResult
import com.eygraber.date_time_input.common.generateLocalizedMonthNames
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.time.LocalDate
import java.util.concurrent.CopyOnWriteArrayList

internal class DateInputMonthView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null
) : TextInputLayout(
  context.createDateInputWrapper(R.attr.textInputExposedDropdownMenuStyle),
  attrs
)

internal class DateInputTextLayoutView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null
) : TextInputLayout(
  context.createDateInputWrapper(R.attr.textInputStyle),
  attrs
)

class DateInputView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {
  fun interface OnDateChangedListener {
    fun onDateChanged(dateChangeResult: DateResult)
  }

  private val flowView: Flow
  private val monthContainerView: TextInputLayout
  private val monthView: AutoCompleteTextView
  private val monthLabelView: TextView
  private val dayContainerView: TextInputLayout
  private val dayView: TextInputEditText
  private val dayLabelView: TextView
  private val yearContainerView: TextInputLayout
  private val yearView: TextInputEditText
  private val yearLabelView: TextView
  private val errorView: TextView

  private val dateChangedListeners = CopyOnWriteArrayList<OnDateChangedListener>()

  private var selectedMonth = INVALID_MONTH

  var monthLabel: CharSequence
    get() = monthLabelView.text
    set(value) {
      monthLabelView.text = value
    }

  var dayLabel: CharSequence
    get() = dayLabelView.text
    set(value) {
      dayLabelView.text = value
    }

  var yearLabel: CharSequence
    get() = yearLabelView.text
    set(value) {
      yearLabelView.text = value
    }

  override fun setEnabled(enabled: Boolean) {
    super.setEnabled(enabled)
    monthContainerView.isEnabled = enabled
    dayContainerView.isEnabled = enabled
    yearContainerView.isEnabled = enabled
  }

  val selectedDateResult: DateResult
    get() = DateResult.calculateResult(
      minDate = minDate,
      maxDate = maxDate,
      month = selectedMonth.takeIf { it != INVALID_MONTH },
      day = dayView.text.toString().toIntOrNull(),
      year = yearView.text.toString().toIntOrNull()
    )

  var selectedDate: LocalDate?
    get() = selectedDateResult.getOrNull()
    set(value) {
      try {
        suppressChangeNotifications = true

        if(value == null) {
          errorView.text = null
          errorView.isVisible = false
          selectedMonth = INVALID_MONTH
          monthView.text = null
          dayView.text = null
          yearView.text = null
        }
        else {
          selectedMonth = value.monthValue
          monthView.setText(
            monthView.adapter.getItem(value.monthValue - 1).toString(),
            false
          )
          dayView.setText(value.dayOfMonth.toString())
          dayView.setSelection(dayView.length())
          yearView.setText(value.year.toString())
          yearView.setSelection(yearView.length())
        }
      }
      finally {
        suppressChangeNotifications = false
      }
    }

  var error: CharSequence?
    get() = errorView.text.takeIf { it.isNotBlank() }
    set(value) {
      errorView.text = value
      errorView.isVisible = !value.isNullOrBlank()
      monthContainerView.error = " ".takeIf { value != null }
      dayContainerView.error = " ".takeIf { value != null }
      yearContainerView.error = " ".takeIf { value != null }
    }

  var minDate: LocalDate? = null
  var maxDate: LocalDate? = null

  fun addOnDateChangedListener(dateChangedListener: OnDateChangedListener) {
    dateChangedListeners += dateChangedListener
  }

  fun removeOnDateChangedListener(dateChangedListener: OnDateChangedListener) {
    dateChangedListeners -= dateChangedListener
  }

  private var suppressChangeNotifications = false
  private fun notifyDateChangedListeners() {
    if(suppressChangeNotifications) return

    val date = selectedDateResult
    for(listener in dateChangedListeners) {
      listener.onDateChanged(date)
    }
  }

  init {
    LayoutInflater
      .from(context)
      .inflate(R.layout.date_input_view, this, true)

    flowView = findViewById(R.id.date_flow)
    monthContainerView = findViewById(R.id.monthContainer)
    dayContainerView = findViewById(R.id.dayContainer)
    yearContainerView = findViewById(R.id.yearContainer)

    monthView = monthContainerView.findViewById<AutoCompleteTextView>(R.id.month).apply {
      onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
        selectedMonth = position + 1
        notifyDateChangedListeners()
      }
    }

    monthLabelView = monthContainerView.findViewById(R.id.monthLabel)

    dayView = dayContainerView.findViewById<TextInputEditText>(R.id.day).apply {
      filters += OnlyDigitsFilter

      doAfterTextChanged {
        notifyDateChangedListeners()
      }
    }

    dayLabelView = dayContainerView.findViewById(R.id.dayLabel)

    yearView = yearContainerView.findViewById<TextInputEditText>(R.id.year).apply {
      filters += OnlyDigitsFilter

      doAfterTextChanged {
        notifyDateChangedListeners()
      }
    }

    yearLabelView = yearContainerView.findViewById(R.id.yearLabel)

    errorView = findViewById(R.id.error)

    styledAttr(attrs, R.styleable.DateInputView, R.attr.dateInputViewStyle) {
      val enabled = getBoolean(R.styleable.DateInputView_android_enabled, true)
      isEnabled = enabled

      val horizontalGap = getDimensionPixelSize(
        R.styleable.DateInputView_dateInputView_horizontalGap,
        -1
      )
      if(horizontalGap > -1) {
        flowView.setHorizontalGap(horizontalGap)
      }

      val verticalGap = getDimensionPixelSize(
        R.styleable.DateInputView_dateInputView_verticalGap,
        -1
      )
      if(verticalGap > -1) {
        flowView.setVerticalGap(verticalGap)
      }

      flowView.setHorizontalBias(
        getFloat(R.styleable.DateInputView_dateInputView_horizontalBias, 0F)
      )

      flowView.setVerticalBias(
        getFloat(R.styleable.DateInputView_dateInputView_verticalBias, 0F)
      )

      flowView.setHorizontalStyle(
        getInt(R.styleable.DateInputView_dateInputView_flowStyle, Flow.CHAIN_SPREAD_INSIDE)
      )

      flowView.setHorizontalAlign(
        getInt(R.styleable.DateInputView_dateInputView_flowAlign, Flow.HORIZONTAL_ALIGN_START)
      )

      monthLabelView.text = getString(R.styleable.DateInputView_dateInputView_monthLabel)
        ?: resources.getString(R.string.date_input_view_month_label)

      dayLabelView.text = getString(R.styleable.DateInputView_dateInputView_dayLabel)
        ?: resources.getString(R.string.date_input_view_day_label)

      yearLabelView.text = getString(R.styleable.DateInputView_dateInputView_yearLabel)
        ?: resources.getString(R.string.date_input_view_year_label)

      getDimensionPixelSize(R.styleable.DateInputView_dateInputView_errorMarginStart, -1).let { margin ->
        if(margin >= 0) {
          errorView.updateLayoutParams<LayoutParams> {
            marginStart = margin
          }
        }
      }

      monthView.apply {
        val monthNames =
          when(
            val resId =
              getResourceId(R.styleable.DateInputView_dateInputView_monthNames, -1)
          ) {
            -1 -> generateLocalizedMonthNames()
            else -> resources.getStringArray(resId).toList()
          }

        setAdapter(
          ArrayAdapter(context, android.R.layout.simple_list_item_1, monthNames)
        )
      }
    }
  }

  companion object {
    private const val INVALID_MONTH = -1

    private val OnlyDigitsFilter = InputFilter { source, start, end, _, _, _ ->
      with(StringBuilder()) {
        for(i in start until end) {
          if(source[i].isDigit()) {
            append(source[i])
          }
        }

        when(length) {
          end - start -> null
          else -> toString()
        }
      }
    }
  }
}
