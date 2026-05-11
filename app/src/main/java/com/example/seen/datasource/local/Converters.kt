package com.example.seen.datasource.local

import androidx.room.TypeConverter
import com.example.seen.domain.model.entites.SelectedMedication
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

    private val gson = Gson()

    @TypeConverter
    fun fromMedicationList(
        medications: List<SelectedMedication>?
    ): String? {

        return gson.toJson(medications)
    }

    @TypeConverter
    fun toMedicationList(
        medicationsString: String?
    ): List<SelectedMedication>? {

        if (medicationsString == null) return emptyList()

        val type =
            object : TypeToken<List<SelectedMedication>>() {}.type

        return gson.fromJson(medicationsString, type)
    }
}