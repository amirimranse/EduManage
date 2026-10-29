package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "classes")
data class ClassEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val className: String
)

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val groupName: String,
    val classId: Int // Links to ClassEntity
)

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val fatherName: String,
    val age: Int,
    val dob: String,
    val contactNumber: String,
    val address: String,
    val classId: Int, // Links to ClassEntity
    val groupId: Int, // Links to GroupEntity
    val admissionDate: String
)

@Entity(tableName = "fee_history")
data class FeeHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: Int,
    val monthName: String, // e.g. "June 2026"
    val amount: Double,
    val isPaid: Boolean,
    val paymentDate: String? = null
)

@Entity(tableName = "attendance")
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: Int,
    val dateString: String, // yyyy-MM-dd
    val status: String // "Present", "Absent", "Late"
)

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val passwordHash: String,
    val fullName: String,
    val role: String // "Admin", "Teacher"
)
