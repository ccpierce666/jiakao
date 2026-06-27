package cn.xmfengxing.kao.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class PracticeProgress(
    val selectedAnswers: Set<Int>,
    val correct: Boolean
)

data class PracticeStatistics(
    val total: Int,
    val answered: Int,
    val correct: Int,
    val wrong: Int,
    val favorites: Int
) {
    val unpracticed: Int get() = (total - answered).coerceAtLeast(0)
    val accuracy: Int get() = if (answered == 0) 0 else correct * 100 / answered
}

class PracticeProgressRepository(
    context: Context,
    subject: ExamSubject = ExamSubject.SubjectOne
) {
    private val helper = PracticeProgressDatabase(context.applicationContext)
    private val bankKey = subject.bankKey

    fun getStatistics(questionIds: Collection<Long>): PracticeStatistics {
        val validQuestionIds = questionIds.toHashSet()
        var answered = 0
        var correct = 0
        var wrong = 0

        val progressStatistics = helper.readableDatabase.rawQuery(
            """
            SELECT question_id, correct
            FROM question_progress
            WHERE bank_key = ?
            """.trimIndent(),
            arrayOf(bankKey)
        ).use { cursor ->
            while (cursor.moveToNext()) {
                if (cursor.getLong(0) !in validQuestionIds) continue
                answered++
                if (cursor.getInt(1) == 1) correct++ else wrong++
            }
            Triple(answered, correct, wrong)
        }
        val favorites = getFavoriteQuestionIds(questionIds).size
        return PracticeStatistics(
            total = validQuestionIds.size,
            answered = progressStatistics.first,
            correct = progressStatistics.second,
            wrong = progressStatistics.third,
            favorites = favorites
        )
    }

    fun getProgress(questionId: Long): PracticeProgress? {
        return helper.readableDatabase.rawQuery(
            """
            SELECT selected_answers, correct
            FROM question_progress
            WHERE bank_key = ? AND question_id = ?
            """.trimIndent(),
            arrayOf(bankKey, questionId.toString())
        ).use { cursor ->
            if (!cursor.moveToFirst()) return@use null
            PracticeProgress(
                selectedAnswers = decodeAnswers(cursor.getString(0)),
                correct = cursor.getInt(1) == 1
            )
        }
    }

    fun getUnpracticedQuestionIds(questionIds: Collection<Long>): List<Long> {
        val answeredIds = getAnsweredQuestionIds()
        return questionIds.filterNot(answeredIds::contains)
    }

    fun getWrongQuestionIds(questionIds: Collection<Long>): List<Long> {
        val validQuestionIds = questionIds.toHashSet()
        return helper.readableDatabase.rawQuery(
            """
            SELECT question_id
            FROM question_progress
            WHERE bank_key = ? AND correct = 0
            """.trimIndent(),
            arrayOf(bankKey)
        ).use { cursor ->
            buildSet {
                while (cursor.moveToNext()) {
                    cursor.getLong(0).takeIf(validQuestionIds::contains)?.let(::add)
                }
            }.let { wrongIds -> questionIds.filter(wrongIds::contains) }
        }
    }

    fun getFavoriteQuestionIds(questionIds: Collection<Long>): List<Long> {
        val validQuestionIds = questionIds.toHashSet()
        return helper.readableDatabase.rawQuery(
            """
            SELECT question_id
            FROM question_favorite
            WHERE bank_key = ?
            """.trimIndent(),
            arrayOf(bankKey)
        ).use { cursor ->
            buildSet {
                while (cursor.moveToNext()) {
                    cursor.getLong(0).takeIf(validQuestionIds::contains)?.let(::add)
                }
            }.let { favoriteIds -> questionIds.filter(favoriteIds::contains) }
        }
    }

    fun isFavorite(questionId: Long): Boolean {
        return helper.readableDatabase.rawQuery(
            """
            SELECT 1
            FROM question_favorite
            WHERE bank_key = ? AND question_id = ?
            LIMIT 1
            """.trimIndent(),
            arrayOf(bankKey, questionId.toString())
        ).use { cursor -> cursor.moveToFirst() }
    }

    fun setFavorite(questionId: Long, favorite: Boolean) {
        if (favorite) {
            val values = ContentValues().apply {
                put("bank_key", bankKey)
                put("question_id", questionId)
                put("created_at", System.currentTimeMillis())
            }
            helper.writableDatabase.insertWithOnConflict(
                "question_favorite",
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
            )
        } else {
            helper.writableDatabase.delete(
                "question_favorite",
                "bank_key = ? AND question_id = ?",
                arrayOf(bankKey, questionId.toString())
            )
        }
    }

    fun saveAnswer(questionId: Long, selectedAnswers: Set<Int>, correct: Boolean) {
        val values = ContentValues().apply {
            put("bank_key", bankKey)
            put("question_id", questionId)
            put("selected_answers", encodeAnswers(selectedAnswers))
            put("correct", if (correct) 1 else 0)
            put("updated_at", System.currentTimeMillis())
        }
        helper.writableDatabase.insertWithOnConflict(
            "question_progress",
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun getLastQuestionId(): Long? {
        return helper.readableDatabase.rawQuery(
            "SELECT last_question_id FROM practice_state WHERE bank_key = ?",
            arrayOf(bankKey)
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.getLong(0) else null
        }
    }

    fun saveLastQuestion(questionId: Long) {
        val values = ContentValues().apply {
            put("bank_key", bankKey)
            put("last_question_id", questionId)
        }
        helper.writableDatabase.insertWithOnConflict(
            "practice_state",
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun clear() {
        helper.writableDatabase.delete(
            "question_progress",
            "bank_key = ?",
            arrayOf(bankKey)
        )
        helper.writableDatabase.delete(
            "practice_state",
            "bank_key = ?",
            arrayOf(bankKey)
        )
    }

    fun clearWrongQuestions() {
        helper.writableDatabase.delete(
            "question_progress",
            "bank_key = ? AND correct = 0",
            arrayOf(bankKey)
        )
    }

    private fun encodeAnswers(answers: Set<Int>): String =
        answers.sorted().joinToString(",")

    private fun decodeAnswers(value: String?): Set<Int> =
        value.orEmpty()
            .split(",")
            .mapNotNull(String::toIntOrNull)
            .toSet()

    private fun getAnsweredQuestionIds(): Set<Long> {
        return helper.readableDatabase.rawQuery(
            "SELECT question_id FROM question_progress WHERE bank_key = ?",
            arrayOf(bankKey)
        ).use { cursor ->
            buildSet {
                while (cursor.moveToNext()) add(cursor.getLong(0))
            }
        }
    }

}

private class PracticeProgressDatabase(context: Context) :
    SQLiteOpenHelper(context, "practice_progress.db", null, 2) {

    override fun onCreate(database: SQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE question_progress (
                bank_key TEXT NOT NULL,
                question_id INTEGER NOT NULL,
                selected_answers TEXT NOT NULL,
                correct INTEGER NOT NULL,
                favorite INTEGER NOT NULL DEFAULT 0,
                updated_at INTEGER NOT NULL,
                PRIMARY KEY (bank_key, question_id)
            )
            """.trimIndent()
        )
        createFavoriteTable(database)
        database.execSQL(
            """
            CREATE TABLE practice_state (
                bank_key TEXT PRIMARY KEY,
                last_question_id INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(
        database: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        if (oldVersion < 2) createFavoriteTable(database)
    }

    private fun createFavoriteTable(database: SQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS question_favorite (
                bank_key TEXT NOT NULL,
                question_id INTEGER NOT NULL,
                created_at INTEGER NOT NULL,
                PRIMARY KEY (bank_key, question_id)
            )
            """.trimIndent()
        )
    }
}
