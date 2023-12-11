package com.canque.todo.adapters

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.RecyclerView
import com.canque.todo.R
import com.canque.todo.TaskActivity
import com.canque.todo.constants.Constants
import com.canque.todo.databinding.ItemTaskBinding
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
                Toast.makeText(activity, "Sorry, this feature is incomplete.", Toast.LENGTH_SHORT).show()
            }
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