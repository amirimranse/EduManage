package com.example.data.db

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ClassDao {
    @Query("SELECT * FROM classes ORDER BY className ASC")
    fun getAllClassesFlow(): Flow<List<ClassEntity>>

    @Query("SELECT * FROM classes")
    suspend fun getAllClasses(): List<ClassEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(classEntity: ClassEntity): Long

    @Update
    suspend fun updateClass(classEntity: ClassEntity)

    @Delete
    suspend fun deleteClass(classEntity: ClassEntity)

    @Query("SELECT * FROM classes WHERE id = :id LIMIT 1")
    suspend fun getClassById(id: Int): ClassEntity?
}

@Dao
interface GroupDao {
    @Query("SELECT * FROM `groups` ORDER BY groupName ASC")
    fun getAllGroupsFlow(): Flow<List<GroupEntity>>

    @Query("SELECT * FROM `groups`")
    suspend fun getAllGroups(): List<GroupEntity>

    @Query("SELECT * FROM `groups` WHERE classId = :classId ORDER BY groupName ASC")
    fun getGroupsByClassFlow(classId: Int): Flow<List<GroupEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(groupEntity: GroupEntity): Long

    @Update
    suspend fun updateGroup(groupEntity: GroupEntity)

    @Delete
    suspend fun deleteGroup(groupEntity: GroupEntity)
}

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY name ASC")
    fun getAllStudentsFlow(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
    suspend fun getStudentById(id: Int): StudentEntity?

    @Query("SELECT * FROM students WHERE classId = :classId")
    fun getStudentsByClassFlow(classId: Int): Flow<List<StudentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity): Long

    @Update
    suspend fun updateStudent(student: StudentEntity)

    @Delete
    suspend fun deleteStudent(student: StudentEntity)
}

@Dao
interface FeeHistoryDao {
    @Query("SELECT * FROM fee_history ORDER BY id DESC")
    fun getAllFeesFlow(): Flow<List<FeeHistoryEntity>>

    @Query("SELECT * FROM fee_history WHERE studentId = :studentId ORDER BY monthName DESC")
    fun getFeesForStudentFlow(studentId: Int): Flow<List<FeeHistoryEntity>>

    @Query("SELECT * FROM fee_history WHERE studentId = :studentId")
    suspend fun getFeesForStudent(studentId: Int): List<FeeHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFee(fee: FeeHistoryEntity): Long

    @Update
    suspend fun updateFee(fee: FeeHistoryEntity)

    @Delete
    suspend fun deleteFee(fee: FeeHistoryEntity)
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance ORDER BY dateString DESC")
    fun getAllAttendanceFlow(): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE studentId = :studentId ORDER BY dateString DESC")
    fun getAttendanceForStudentFlow(studentId: Int): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE dateString = :dateString")
    suspend fun getAttendanceByDate(dateString: String): List<AttendanceEntity>

    @Query("SELECT * FROM attendance WHERE studentId = :studentId AND dateString = :dateString LIMIT 1")
    suspend fun getStudentAttendanceForDate(studentId: Int, dateString: String): AttendanceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: AttendanceEntity): Long

    @Update
    suspend fun updateAttendance(attendance: AttendanceEntity)

    @Delete
    suspend fun deleteAttendance(attendance: AttendanceEntity)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Query("DELETE FROM users WHERE username = :username")
    suspend fun deleteUserByUsername(username: String)

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
}

@Database(
    entities = [
        ClassEntity::class,
        GroupEntity::class,
        StudentEntity::class,
        FeeHistoryEntity::class,
        AttendanceEntity::class,
        UserEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun classDao(): ClassDao
    abstract fun groupDao(): GroupDao
    abstract fun studentDao(): StudentDao
    abstract fun feeHistoryDao(): FeeHistoryDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "student_management_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
