package com.canque.todo.datastore

import android.content.Context
import com.canque.todo.constants.Constants
import com.canque.todo.models.Task
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SharedPref(context: Context) {

    private val sharedPref = context
        .getSharedPreferences(Constants.GENERAL, Context.MODE_PRIVATE)

    private val gson = Gson()

    var lastAssignedId: Int
        set(light) = sharedPref.edit()
            .putInt(Constants.PARAM_LAST_ASSIGNED_ID, lastAssignedId).apply()
        get() = sharedPref
            .getInt(Constants.PARAM_LAST_ASSIGNED_ID, 0)

    // Add a method to save a list of your data model
    fun saveTaskList(dataList: List<Task>) {
        val json = gson.toJson(dataList)
        sharedPref.edit().putString(Constants.PARAM_DATA_LIST, json).apply()
    }

    // Add a method to load a list of your data model
    fun loadTaskList(): List<Task> {
        val json = sharedPref.getString(Constants.PARAM_DATA_LIST, null)
        return if (json != null) {
            val type = object : TypeToken<List<Task>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }
}