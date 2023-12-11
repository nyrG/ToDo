package com.canque.todo.models

import com.canque.todo.datastore.SharedPref

data class Task(
    val id: String,
    var name: String,
    var description: String?,
    var dueDate: String?,
)


