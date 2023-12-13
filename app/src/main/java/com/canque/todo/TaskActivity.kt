package com.canque.todo

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.DatePicker
import android.widget.LinearLayout
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.canque.todo.constants.Constants
import com.canque.todo.databinding.ActivityTaskBinding
import com.canque.todo.datastore.SharedPref
import com.canque.todo.models.Task
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TaskActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private lateinit var binding: ActivityTaskBinding
    private var selectedYear = 0
    private var selectedMonth = 0
    private var selectedDay = 0
    private var selectedDateTime = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initStatusBar()
        val sharedPref = SharedPref(this)
        val tasks = sharedPref.loadTaskList().toMutableList()
        val id = intent.getStringExtra(Constants.PARAM_ID)

        binding.detailsButton.setOnClickListener() {
            showDialog()
        }

        binding.nameContainer.setOnClickListener() {
            binding.nameEditText.requestFocus()
            binding.nameEditText.setSelection(binding.nameEditText.text.length)
            showKeyboard(binding.nameEditText)
        }

        binding.descriptionContainer.setOnClickListener() {
            binding.descriptionEditText.requestFocus()
            binding.descriptionEditText.setSelection(binding.descriptionEditText.text.length)
            showKeyboard(binding.descriptionEditText)
        }

        binding.addDateTimeContainer.setOnClickListener() {
            showDatePicker()
        }

        // Add a TextWatcher to automatically update the data model
        binding.nameEditText.addTextChangedListener(object : TextWatcher {
            inline fun <reified T> MutableList<T>.update(condition: (T) -> Boolean, update: (T) -> T) {
                val index = indexOfFirst(condition)
                if (index != -1) {
                    this[index] = update(this[index])
                }
            }
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                // Update the variable in your data model as the text changes
                tasks.update({ it.id == id }) {
                    it.copy(name = charSequence.toString())
                }
                sharedPref.saveTaskList(tasks)
            }

            override fun afterTextChanged(editable: Editable?) {}
        })
        binding.descriptionEditText.addTextChangedListener(object : TextWatcher {
            inline fun <reified T> MutableList<T>.update(condition: (T) -> Boolean, update: (T) -> T) {
                val index = indexOfFirst(condition)
                if (index != -1) {
                    this[index] = update(this[index])
                }
            }
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                // Update the variable in your data model as the text changes
                tasks.update({ it.id == id }) {
                    it.copy(description = charSequence.toString())
                }
                sharedPref.saveTaskList(tasks)
            }

            override fun afterTextChanged(editable: Editable?) {}
        })
    }
    override fun onResume() {
        super.onResume()
        getTask()
    }
    private fun getTask() {
        val task = getTaskById()

        if (task != null) {
            binding.nameEditText.setText(task.name)
            binding.descriptionEditText.setText(task.description)
            binding.addDateTimeTextView.setText(task.dueDate)
        }
    }
    private fun getTaskById(): Task? {
        val id = intent.getStringExtra(Constants.PARAM_ID)
        val sharedPref = SharedPref(this)
        val tasks = sharedPref.loadTaskList().toMutableList()
        val task: Task? = tasks.find { it.id == id }

        return task
    }
    fun updateDateTimeById(targetId: String, newDateTime: String) {
        val sharedPref = SharedPref(this)
        val tasks = sharedPref.loadTaskList().toMutableList()
        // Find the element with the specified id
        val task = tasks.find { it.id == targetId }

        // Update the name if the element is found
        task?.let {
            it.dueDate = newDateTime
        }

        sharedPref.saveTaskList(tasks)
    }
    private fun showDialog() {
        val sheet = Dialog(this)
        sheet.requestWindowFeature(Window.FEATURE_NO_TITLE)
        sheet.setContentView(R.layout.bottom_sheet)

        val deleteLayout: LinearLayout = sheet.findViewById(R.id.layoutDelete)

        deleteLayout.setOnClickListener() {
            Toast.makeText(this, "This is a standard AlertDialog.", Toast.LENGTH_SHORT).show()
            val builder = AlertDialog.Builder(this, R.style.RoundedCornersAlertDialog)
            builder
                .setTitle("Delete task?")
                .setMessage("This can’t be undone and it will be removed from your to-do list.")
                .setPositiveButton("Delete"){ dialog, _ ->
                    deleteTask()
                    finish()
                }
                .setNegativeButton("Cancel"){dialog,_ ->
                    dialog.dismiss()
                }
            val alertDialog = builder.create()
            alertDialog.setOnShowListener {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    ?.setTextColor(ContextCompat.getColor(this, android.R.color.black))
                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                    ?.setTextColor(ContextCompat.getColor(this, android.R.color.black))
            }
            alertDialog.show()
            sheet.dismiss()
        }

        sheet.show()
        sheet.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        sheet.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        sheet.window?.attributes?.windowAnimations = R.style.DialogAnimation
        sheet.window?.setGravity(Gravity.BOTTOM)
    }
    private fun deleteTask() {
        val id = intent.getStringExtra(Constants.PARAM_ID)
        val sharedPref = SharedPref(this)
        var tasks = sharedPref.loadTaskList().toMutableList()

        tasks = tasks.filter { it.id != id }.toMutableList()
        sharedPref.saveTaskList(tasks)
    }
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, this, year, month, day)
        datePickerDialog.show()
        Toast.makeText(this, "This is a DatePicker dialog.", Toast.LENGTH_SHORT).show()
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        // Save the selected date
        selectedYear = year
        selectedMonth = month
        selectedDay = dayOfMonth

        // Prompt the user with an AlertDialog
        showTimePickerAlertDialog()
    }
    private fun showTimePickerAlertDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Set Time")
        alertDialogBuilder.setMessage("Do you want to set a time?")
        alertDialogBuilder.setPositiveButton("Yes") { _, _ ->
            // User wants to set a time, show TimePickerDialog
            showTimePicker()
        }
        alertDialogBuilder.setNegativeButton("No") { _, _ ->
            // User doesn't want to set a time, update the TextView with date only
            updateDateTime()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(this, this, hour, minute, false)
        timePickerDialog.show()
        Toast.makeText(this, "This is a TimePicker dialog.", Toast.LENGTH_SHORT).show()
    }
    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        // Handle the selected date and time
        updateDateTime(hourOfDay, minute)
    }
    private fun updateDateTime(hourOfDay: Int = 0, minute: Int = 0) {
        // Format the date with "dd/MM/yyyy" format
        val selectedDate = Calendar.getInstance().apply {
            set(selectedYear, selectedMonth, selectedDay, hourOfDay, minute)
        }.time

        val id = intent.getStringExtra(Constants.PARAM_ID)

        val dateFormat = SimpleDateFormat("dd, MMM, yyyy")
        val formattedDate = dateFormat.format(selectedDate)

        // Concatenate time if available
        val timeFormat = SimpleDateFormat("h:mm a")
        val formattedTime = if (hourOfDay != 0 || minute != 0) ", " + timeFormat.format(selectedDate) else ""

        selectedDateTime = "$formattedDate$formattedTime"

        // Update the TextView
        binding.addDateTimeTextView.text = selectedDateTime

        Log.d("Selected Date Time","$selectedDateTime")
        updateDateTimeById(id!!, selectedDateTime)
    }
    private fun showKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }
    fun onBackButtonClick(view: View) {
        // Handle back button click, e.g., navigate back
        onBackPressed()
    }
    private fun initStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        }
    }
}