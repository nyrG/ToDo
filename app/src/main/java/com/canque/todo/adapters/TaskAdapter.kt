package com.canque.todo.adapters

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.RecyclerView
import com.canque.todo.R
import com.canque.todo.TaskActivity
import com.canque.todo.constants.Constants
import com.canque.todo.databinding.ItemTaskBinding
import com.canque.todo.datastore.SharedPref
import com.canque.todo.models.Task

class TaskAdapter(
    private val activity: Activity,
    private val taskList: List<Task>,
): RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(
        private val activity: Activity,
        private val binding: ItemTaskBinding,
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(task: Task) {
            binding.name.text = task.name
            binding.item.setOnClickListener {
                val intent = Intent(activity, TaskActivity::class.java)
                intent.putExtra(Constants.PARAM_ID, task.id)
                activity.startActivity(intent)
            }
            binding.details.setOnClickListener() {
                showDialog(task, activity)
            }
        }
        private fun showDialog(task: Task, context: Context) {
            val sheet = Dialog(context)
            sheet.requestWindowFeature(Window.FEATURE_NO_TITLE)
            sheet.setContentView(R.layout.bottom_sheet)

            val deleteLayout: LinearLayout = sheet.findViewById(R.id.layoutDelete)

            deleteLayout.setOnClickListener() {
                Toast.makeText(context, "This is a standard AlertDialog.", Toast.LENGTH_SHORT).show()
                val builder = androidx.appcompat.app.AlertDialog.Builder(context, R.style.RoundedCornersAlertDialog)
                builder
                    .setTitle("Delete task?")
                    .setMessage("This can’t be undone and it will be removed from your to-do list.")
                    .setPositiveButton("Delete"){ dialog, _ ->
                        val id = task.id
                        val sharedPref = SharedPref(context)
                        var tasks = sharedPref.loadTaskList().toMutableList()

                        tasks = tasks.filter { it.id != id }.toMutableList()
                        sharedPref.saveTaskList(tasks)
                    }
                    .setNegativeButton("Cancel"){dialog,_ ->
                        dialog.dismiss()
                    }
                val alertDialog = builder.create()
                alertDialog.setOnShowListener {
                    alertDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                        ?.setTextColor(ContextCompat.getColor(context, android.R.color.black))
                    alertDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)
                        ?.setTextColor(ContextCompat.getColor(context, android.R.color.black))
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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemTaskBinding.inflate(
            inflater,
            parent,
            false,
        )
        return TaskViewHolder(activity, binding)
    }

    override fun getItemCount() = taskList.size

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(taskList[position])
    }
}