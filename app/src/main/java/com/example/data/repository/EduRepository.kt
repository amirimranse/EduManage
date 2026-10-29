package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.db.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

data class StudentWithDetails(
    val student: StudentEntity,
    val className: String,
    val groupName: String
)

class EduRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val classDao = db.classDao()
    private val groupDao = db.groupDao()
    private val studentDao = db.studentDao()
    private val feeHistoryDao = db.feeHistoryDao()
    private val attendanceDao = db.attendanceDao()
    private val userDao = db.userDao()

    // Flow getters
    val classes: Flow<List<ClassEntity>> = classDao.getAllClassesFlow()
    val groups: Flow<List<GroupEntity>> = groupDao.getAllGroupsFlow()
    val students: Flow<List<StudentEntity>> = studentDao.getAllStudentsFlow()
    val fees: Flow<List<FeeHistoryEntity>> = feeHistoryDao.getAllFeesFlow()
    val attendance: Flow<List<AttendanceEntity>> = attendanceDao.getAllAttendanceFlow()

    // Combine Student with Class and Group
    val studentsWithDetails: Flow<List<StudentWithDetails>> = combine(
        students,
        classes,
        groups
    ) { studentList, classList, groupList ->
        val classMap = classList.associateBy { it.id }
        val groupMap = groupList.associateBy { it.id }

        studentList.map { student ->
            StudentWithDetails(
                student = student,
                className = classMap[student.classId]?.className ?: "Unknown Class",
                groupName = groupMap[student.groupId]?.groupName ?: "Unknown Group"
            )
        }
    }

    // Dynamic filtering or querying helpers
    fun getFeesForStudent(studentId: Int): Flow<List<FeeHistoryEntity>> =
        feeHistoryDao.getFeesForStudentFlow(studentId)

    fun getAttendanceForStudent(studentId: Int): Flow<List<AttendanceEntity>> =
        attendanceDao.getAttendanceForStudentFlow(studentId)

    // DB Operations
    suspend fun insertClass(classEntity: ClassEntity) = classDao.insertClass(classEntity)
    suspend fun updateClass(classEntity: ClassEntity) = classDao.updateClass(classEntity)
    suspend fun deleteClass(classEntity: ClassEntity) = classDao.deleteClass(classEntity)

    suspend fun insertGroup(groupEntity: GroupEntity) = groupDao.insertGroup(groupEntity)
    suspend fun updateGroup(groupEntity: GroupEntity) = groupDao.updateGroup(groupEntity)
    suspend fun deleteGroup(groupEntity: GroupEntity) = groupDao.deleteGroup(groupEntity)

    suspend fun insertStudent(student: StudentEntity) = studentDao.insertStudent(student)
    suspend fun updateStudent(student: StudentEntity) = studentDao.updateStudent(student)
    suspend fun deleteStudent(student: StudentEntity) = studentDao.deleteStudent(student)

    suspend fun insertFee(fee: FeeHistoryEntity) = feeHistoryDao.insertFee(fee)
    suspend fun updateFee(fee: FeeHistoryEntity) = feeHistoryDao.updateFee(fee)
    suspend fun deleteFee(fee: FeeHistoryEntity) = feeHistoryDao.deleteFee(fee)

    suspend fun insertAttendance(attendance: AttendanceEntity) = attendanceDao.insertAttendance(attendance)
    suspend fun updateAttendance(attendance: AttendanceEntity) = attendanceDao.updateAttendance(attendance)
    suspend fun deleteAttendance(attendance: AttendanceEntity) = attendanceDao.deleteAttendance(attendance)

    suspend fun getStudentAttendanceForDate(studentId: Int, dateString: String) =
        attendanceDao.getStudentAttendanceForDate(studentId, dateString)

    // Auth helpers
    suspend fun getUserByUsername(username: String): UserEntity? = userDao.getUserByUsername(username)

    // Initial Seeding
    suspend fun seedIfNeeded() {
        try {
            // Delete legacy/obsolete credentials to satisfy strict security requirements
            userDao.deleteUserByUsername("admin")
            userDao.deleteUserByUsername("teacher")

            // Always ensure the secure user exists
            val secureUser = userDao.getUserByUsername("3210383002631")
            if (secureUser == null) {
                userDao.insertUser(
                    UserEntity(
                        username = "3210383002631",
                        passwordHash = hashPassword("Amir"),
                        fullName = "Aamir Khan",
                        role = "Admin"
                    )
                )
            }

            // Seed classes, groups, and students only if they do not exist
            val existingClasses = classDao.getAllClasses()
            if (existingClasses.isEmpty()) {
                Log.d("EduRepository", "Seeding database with default classes, groups, students")

                // Seed Classes
                val classA = classDao.insertClass(ClassEntity(className = "Grade 9"))
                val classB = classDao.insertClass(ClassEntity(className = "Grade 10"))
                val classC = classDao.insertClass(ClassEntity(className = "Grade 11"))
                val classD = classDao.insertClass(ClassEntity(className = "Grade 12"))

                // Seed Groups (Sections) linked to classes
                val grpA1 = groupDao.insertGroup(GroupEntity(groupName = "Section A (Science)", classId = classA.toInt()))
                val grpA2 = groupDao.insertGroup(GroupEntity(groupName = "Section B (Arts)", classId = classA.toInt()))
                val grpB1 = groupDao.insertGroup(GroupEntity(groupName = "Section A", classId = classB.toInt()))
                val grpC1 = groupDao.insertGroup(GroupEntity(groupName = "Medical Group", classId = classC.toInt()))
                val grpC2 = groupDao.insertGroup(GroupEntity(groupName = "Engineering Group", classId = classC.toInt()))
                val grpD1 = groupDao.insertGroup(GroupEntity(groupName = "Commerce Group", classId = classD.toInt()))

                // Seed Students
                val st1 = StudentEntity(
                    name = "Aamir Khan",
                    fatherName = "Inam Khan",
                    age = 16,
                    dob = "2010-04-12",
                    contactNumber = "+1 (555) 732-2831",
                    address = "742 Evergreen Terrace, Springfield",
                    classId = classA.toInt(),
                    groupId = grpA1.toInt(),
                    admissionDate = "2024-09-01"
                )
                val st1Id = studentDao.insertStudent(st1)

                val st2 = StudentEntity(
                    name = "Fatima Naqvi",
                    fatherName = "Zafar Naqvi",
                    age = 17,
                    dob = "2009-11-20",
                    contactNumber = "+1 (555) 349-4182",
                    address = "12 Main Street, Emerald City",
                    classId = classB.toInt(),
                    groupId = grpB1.toInt(),
                    admissionDate = "2024-09-01"
                )
                val st2Id = studentDao.insertStudent(st2)

                val st3 = StudentEntity(
                    name = "David Miller",
                    fatherName = "Robert Miller",
                    age = 18,
                    dob = "2008-01-15",
                    contactNumber = "+1 (555) 982-1923",
                    address = "321 Oakwood Avenue, Brookside",
                    classId = classC.toInt(),
                    groupId = grpC2.toInt(),
                    admissionDate = "2023-09-01"
                )
                val st3Id = studentDao.insertStudent(st3)

                val st4 = StudentEntity(
                    name = "Emily Watson",
                    fatherName = "James Watson",
                    age = 17,
                    dob = "2009-07-04",
                    contactNumber = "+1 (555) 234-5678",
                    address = "98 Pine Crescent, Riverside",
                    classId = classC.toInt(),
                    groupId = grpC1.toInt(),
                    admissionDate = "2024-02-15"
                )
                val st4Id = studentDao.insertStudent(st4)

                val st5 = StudentEntity(
                    name = "Raj Patel",
                    fatherName = "Amit Patel",
                    age = 18,
                    dob = "2008-08-11",
                    contactNumber = "+1 (555) 876-5432",
                    address = "45 Maple Blvd, Metro City",
                    classId = classD.toInt(),
                    groupId = grpD1.toInt(),
                    admissionDate = "2023-09-01"
                )
                val st5Id = studentDao.insertStudent(st5)

                // Seed Fee Histories
                feeHistoryDao.insertFee(FeeHistoryEntity(studentId = st1Id.toInt(), monthName = "June 2026", amount = 150.0, isPaid = true, paymentDate = "2026-06-05"))
                feeHistoryDao.insertFee(FeeHistoryEntity(studentId = st1Id.toInt(), monthName = "May 2026", amount = 150.0, isPaid = true, paymentDate = "2026-05-02"))
                feeHistoryDao.insertFee(FeeHistoryEntity(studentId = st2Id.toInt(), monthName = "June 2026", amount = 155.0, isPaid = false))
                feeHistoryDao.insertFee(FeeHistoryEntity(studentId = st2Id.toInt(), monthName = "May 2026", amount = 155.0, isPaid = true, paymentDate = "2026-05-03"))
                feeHistoryDao.insertFee(FeeHistoryEntity(studentId = st3Id.toInt(), monthName = "June 2026", amount = 160.0, isPaid = true, paymentDate = "2026-06-12"))
                feeHistoryDao.insertFee(FeeHistoryEntity(studentId = st4Id.toInt(), monthName = "June 2026", amount = 160.0, isPaid = false))
                feeHistoryDao.insertFee(FeeHistoryEntity(studentId = st5Id.toInt(), monthName = "June 2026", amount = 165.0, isPaid = true, paymentDate = "2026-06-01"))

                // Seed Attendance records for historical dates and today in 2026-06-20
                val dates = listOf("2026-06-18", "2026-06-19", "2026-06-20")
                val studentsList = listOf(st1Id, st2Id, st3Id, st4Id, st5Id)

                // Let's mark mostly present, some absent and lateness
                for (date in dates) {
                    for (index in studentsList.indices) {
                        val stId = studentsList[index].toInt()
                        val status = when {
                            stId == st2Id.toInt() && date == "2026-06-19" -> "Absent"
                            stId == st4Id.toInt() && date == "2026-06-18" -> "Late"
                            else -> "Present"
                        }
                        attendanceDao.insertAttendance(AttendanceEntity(studentId = stId, dateString = date, status = status))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("EduRepository", "Error seeding database: ", e)
        }
    }

    companion object {
        fun hashPassword(password: String): String {
            return try {
                val digest = java.security.MessageDigest.getInstance("SHA-256")
                val hash = digest.digest(password.toByteArray(Charsets.UTF_8))
                hash.joinToString("") { "%02x".format(it) }
            } catch (e: Exception) {
                password // Fallback to plaintext if hashing fails
            }
        }
    }
}
