package com.canque.todo

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.canque.todo.adapters.TaskAdapter
import com.canque.todo.databinding.ActivityMainBinding
import com.canque.todo.datastore.SharedPref
import com.canque.todo.models.Task

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initStatusBar()
        val sharedPref = SharedPref(this)
        val tasks = sharedPref.loadTaskList().toMutableList()
        binding.counterTextView.text = tasks.size.toString()

        binding.add.setOnClickListener {
            showCustomDialog(tasks)
            Toast.makeText(this, "This is an AlertDialog with input texts.", Toast.LENGTH_SHORT).show()
        }

        Log.d("Task List", "$tasks")

        /*tasks.clear()
        sharedPref.saveTaskList(tasks)*/

        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshList(tasks)
        }
        binding.taskList.layoutManager = LinearLayoutManager(this)
        binding.taskList.adapter = TaskAdapter(this, tasks)
    }
    override fun onResume() {
        val sharedPref = SharedPref(this)
        val tasks = sharedPref.loadTaskList().toMutableList()
        super.onResume()
        refreshList(tasks)
    }
    private fun showCustomDialog(tasks: MutableList<Task>) {
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.layout_custom_dialog, null)

        val editText1 = view.findViewById<EditText>(R.id.nameEditText)
        val editText2 = view.findViewById<EditText>(R.id.descriptionEditText)

        val alertDialogBuilder = AlertDialog.Builder(this, R.style.RoundedCornersAlertDialog)
        alertDialogBuilder.setView(view)
        alertDialogBuilder.setPositiveButton("OK") { dialog, which ->
            // Retrieve the text inputted by the user
            val text1 = editText1.text.toString()
            val text2 = editText2.text.toString()
            // Do something with the input
            val sharedPref = SharedPref(this)
            //tasks = sharedPref.loadTaskList().toMutableList()
            if(!(text1.isNullOrEmpty())) {
                val task = Task( genId(20),"$text1", "$text2", null)
                tasks.add(task)
                sharedPref.saveTaskList(tasks)
                refreshList(tasks)
                Toast.makeText(this, "Task added!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Your task should have a name.", Toast.LENGTH_SHORT).show()
            }
        }
        alertDialogBuilder.setNegativeButton("Cancel") { dialog, which ->
            dialog.dismiss()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.setOnShowListener {
            alertDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                ?.setTextColor(ContextCompat.getColor(this, android.R.color.black))
            alertDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)
                ?.setTextColor(ContextCompat.getColor(this, android.R.color.black))
        }
        alertDialog.show()
    }

    private fun genId(length: Int): String {
        val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    private fun initStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        }
    }

    private fun refreshList(list: MutableList<Task>) {
        val sharedPref = SharedPref(this)
        //val list = sharedPref.loadTaskList().toMutableList()

        list.clear()
        list.addAll(sharedPref.loadTaskList().toMutableList())
        binding.taskList.adapter!!.notifyDataSetChanged()
        binding.counterTextView.text = list.size.toString()
        binding.swipeRefreshLayout.isRefreshing = false
        Log.d("Refreshed Task List", "$list")
    }
}