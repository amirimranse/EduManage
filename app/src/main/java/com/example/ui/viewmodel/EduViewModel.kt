package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.EduRepository
import com.example.data.repository.StudentWithDetails
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// User state
data class AuthState(
    val isLoggedIn: Boolean = false,
    val currentUser: UserEntity? = null,
    val loginError: String? = null
)

// Main ViewModel for EduManage Application
class EduViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = EduRepository(application)

    // Auth state flow
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Core flows from repository
    val classes: StateFlow<List<ClassEntity>> = repository.classes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val groups: StateFlow<List<GroupEntity>> = repository.groups
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studentsWithDetails: StateFlow<List<StudentWithDetails>> = repository.studentsWithDetails
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val fees: StateFlow<List<FeeHistoryEntity>> = repository.fees
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val attendance: StateFlow<List<AttendanceEntity>> = repository.attendance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search and filter parameters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedClassFilter = MutableStateFlow<Int?>(null)
    val selectedClassFilter: StateFlow<Int?> = _selectedClassFilter.asStateFlow()

    // Filtered Students Flow
    val filteredStudents: StateFlow<List<StudentWithDetails>> = combine(
        studentsWithDetails,
        _searchQuery,
        _selectedClassFilter
    ) { studentList, query, classFilter ->
        studentList.filter { item ->
            val matchQuery = query.isBlank() || 
                item.student.name.contains(query, ignoreCase = true) ||
                item.student.fatherName.contains(query, ignoreCase = true) ||
                item.student.id.toString() == query.trim() ||
                item.className.contains(query, ignoreCase = true) ||
                item.groupName.contains(query, ignoreCase = true)

            val matchClass = classFilter == null || item.student.classId == classFilter

            matchQuery && matchClass
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dashboard Statistics Flow
    val dashboardStats = combine(
        studentsWithDetails,
        fees,
        attendance
    ) { studentList, feeList, attendanceList ->
        val totalStudents = studentList.size
        
        // Fee Tracking - Filter current month (e.g. "June 2026")
        val currentMonth = "June 2026"
        val currentMonthFees = feeList.filter { it.monthName == currentMonth }
        
        // We calculate paid/unpaid based on student profiles who have or do not have a paid record for "June 2026"
        val paidStudentIds = currentMonthFees.filter { it.isPaid }.map { it.studentId }.toSet()
        val unpaidCount = totalStudents - paidStudentIds.size
        val paidCount = paidStudentIds.size

        // Attendance Percentage of last 30 days or general
        val totalAttendanceLogs = attendanceList.size
        val presentAttendanceLogs = attendanceList.count { it.status == "Present" || it.status == "Late" }
        val attendancePercentage = if (totalAttendanceLogs > 0) {
            (presentAttendanceLogs.toDouble() / totalAttendanceLogs) * 100.0
        } else {
            100.0
        }

        // Class-wise statistics
        val classWiseCount = studentList.groupBy { it.className }
            .mapValues { it.value.size }

        DashboardStats(
            totalStudents = totalStudents,
            paidCount = paidCount,
            unpaidCount = unpaidCount,
            attendancePercentage = attendancePercentage,
            classWiseCount = classWiseCount,
            totalPendingFeeAmount = feeList.filter { !it.isPaid }.sumOf { it.amount }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    init {
        // Seed database immediately on initialization
        viewModelScope.launch {
            repository.seedIfNeeded()
        }
    }

    // Auth Methods
    fun login(username: String, pword: String) {
        viewModelScope.launch {
            val user = repository.getUserByUsername(username.trim())
            if (user != null && user.passwordHash == EduRepository.hashPassword(pword)) {
                _authState.value = AuthState(isLoggedIn = true, currentUser = user, loginError = null)
            } else {
                _authState.value = AuthState(isLoggedIn = false, currentUser = null, loginError = "Invalid username or password")
            }
        }
    }

    fun logout() {
        _authState.value = AuthState(isLoggedIn = false, currentUser = null, loginError = null)
    }

    fun clearLoginError() {
        _authState.value = _authState.value.copy(loginError = null)
    }

    // Search and filter updates
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateClassFilter(classId: Int?) {
        _selectedClassFilter.value = classId
    }

    // Student CRUD
    fun addStudent(student: StudentEntity, initialMonthlyFee: Double, createFeeMonth: String) {
        viewModelScope.launch {
            val generatedId = repository.insertStudent(student)
            // Automatically initialize a fee history record for this student
            repository.insertFee(
                FeeHistoryEntity(
                    studentId = generatedId.toInt(),
                    monthName = createFeeMonth,
                    amount = initialMonthlyFee,
                    isPaid = false
                )
            )
            // Also register a default present attendance record for today if admitted today
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            repository.insertAttendance(
                AttendanceEntity(
                    studentId = generatedId.toInt(),
                    dateString = todayStr,
                    status = "Present"
                )
            )
        }
    }

    fun updateStudent(student: StudentEntity) {
        viewModelScope.launch {
            repository.updateStudent(student)
        }
    }

    fun deleteStudent(student: StudentEntity) {
        viewModelScope.launch {
            repository.deleteStudent(student)
        }
    }

    // Class CRUD
    fun addClass(className: String) {
        viewModelScope.launch {
            repository.insertClass(ClassEntity(className = className))
        }
    }

    fun deleteClass(classEntity: ClassEntity) {
        viewModelScope.launch {
            repository.deleteClass(classEntity)
        }
    }

    // Group CRUD
    fun addGroup(groupName: String, classId: Int) {
        viewModelScope.launch {
            repository.insertGroup(GroupEntity(groupName = groupName, classId = classId))
        }
    }

    fun deleteGroup(groupEntity: GroupEntity) {
        viewModelScope.launch {
            repository.deleteGroup(groupEntity)
        }
    }

    // Fee History Ops
    fun addFeeRecord(fee: FeeHistoryEntity) {
        viewModelScope.launch {
            repository.insertFee(fee)
        }
    }

    fun recordFeePayment(feeId: Int, isPaid: Boolean) {
        viewModelScope.launch {
            // Find current record
            val currentFee = fees.value.find { it.id == feeId }
            if (currentFee != null) {
                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                repository.updateFee(
                    currentFee.copy(
                        isPaid = isPaid,
                        paymentDate = if (isPaid) todayStr else null
                    )
                )
            }
        }
    }

    fun updateFeeRecord(fee: FeeHistoryEntity) {
        viewModelScope.launch {
            repository.updateFee(fee)
        }
    }

    // Attendance Operations
    fun markAttendance(studentId: Int, dateStr: String, status: String) {
        viewModelScope.launch {
            val existing = repository.getStudentAttendanceForDate(studentId, dateStr)
            if (existing != null) {
                repository.updateAttendance(existing.copy(status = status))
            } else {
                repository.insertAttendance(
                    AttendanceEntity(
                        studentId = studentId,
                        dateString = dateStr,
                        status = status
                    )
                )
            }
        }
    }

    // Helper: calculate attendance stats per student
    fun getStudentAttendanceSummary(studentId: Int): StudentAttendanceSummary {
        val studentLogs = attendance.value.filter { it.studentId == studentId }
        val total = studentLogs.size
        val present = studentLogs.count { it.status == "Present" }
        val late = studentLogs.count { it.status == "Late" }
        val absent = studentLogs.count { it.status == "Absent" }
        val percentage = if (total > 0) {
            ((present + late).toDouble() / total) * 100.0
        } else {
            100.0 // Default perfect attendance
        }
        return StudentAttendanceSummary(
            total = total,
            present = present,
            late = late,
            absent = absent,
            percentage = percentage
        )
    }

    // Report Generator
    fun generateSystemReport(): String {
        val sb = java.lang.StringBuilder()
        sb.append("=========================================\n")
        sb.append("        EDU-MANAGE SYSTEM REPORT         \n")
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        sb.append("Generated Date: $timestamp\n")
        sb.append("=========================================\n\n")

        val stats = dashboardStats.value
        sb.append("1. GENERAL STATISTICS:\n")
        sb.append(" - Total Registrations: ${stats.totalStudents} Students\n")
        sb.append(" - Average School Attendance: ${String.format(Locale.getDefault(), "%.1f", stats.attendancePercentage)}%\n")
        sb.append(" - Active Classes count: ${classes.value.size}\n")
        sb.append(" - Active Sections/Groups count: ${groups.value.size}\n\n")

        sb.append("2. FINANCIAL HEALTH SUMMARY:\n")
        sb.append(" - Total Students Paid (June 2026): ${stats.paidCount}\n")
        sb.append(" - Pending Defaulters (June 2026): ${stats.unpaidCount}\n")
        sb.append(" - Total Outstandings: $${stats.totalPendingFeeAmount}\n\n")

        sb.append("3. CLASS-WISE METRICS:\n")
        if (stats.classWiseCount.isEmpty()) {
            sb.append(" - No registered records.\n")
        } else {
            for ((className, count) in stats.classWiseCount) {
                sb.append(" - $className: $count Students\n")
            }
        }
        sb.append("\n")

        sb.append("4. OUTSTANDING ACCOUNTS DETAILS:\n")
        val unpaidFees = fees.value.filter { !it.isPaid }
        val studentMap = studentsWithDetails.value.associateBy { it.student.id }
        if (unpaidFees.isEmpty()) {
            sb.append(" - Excellent! No outstanding accounts recorded.\n")
        } else {
            for (fee in unpaidFees) {
                val student = studentMap[fee.studentId]
                val nameStr = student?.student?.name ?: "Unknown Student"
                val classStr = student?.className ?: "N/A"
                sb.append(" - $nameStr ($classStr) | month: ${fee.monthName} | amount: $${fee.amount}\n")
            }
        }
        sb.append("\n=========================================\n")
        sb.append("EduManage Report Engine® - All Rights Secured.\n")
        return sb.toString()
    }
}

// Stats objects
data class DashboardStats(
    val totalStudents: Int = 0,
    val paidCount: Int = 0,
    val unpaidCount: Int = 0,
    val attendancePercentage: Double = 100.0,
    val classWiseCount: Map<String, Int> = emptyMap(),
    val totalPendingFeeAmount: Double = 0.0
)

data class StudentAttendanceSummary(
    val total: Int = 0,
    val present: Int = 0,
    val late: Int = 0,
    val absent: Int = 0,
    val percentage: Double = 100.0
)
