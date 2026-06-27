package cn.xmfengxing.kao.data

import android.content.Context

data class MockExamStatistics(
    val examCount: Int,
    val passCount: Int,
    val recentAverageScore: Int,
    val lastWrongCount: Int,
    val predictedPassRate: Int
)

data class MockExamRecord(
    val id: Long,
    val score: Int,
    val totalScore: Int,
    val passScore: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val unansweredCount: Int,
    val questionCount: Int,
    val durationSeconds: Int,
    val completedAt: Long
) {
    val passed: Boolean get() = score >= passScore
}

class MockExamHistoryRepository(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        "mock_exam_history",
        Context.MODE_PRIVATE
    )

    fun saveResult(
        subject: ExamSubject,
        score: Int,
        totalScore: Int,
        passScore: Int,
        correctCount: Int,
        wrongCount: Int,
        unansweredCount: Int,
        questionCount: Int,
        durationSeconds: Int
    ) {
        val prefix = subject.bankKey
        val completedAt = System.currentTimeMillis()
        val records = getRecords(subject).toMutableList().apply {
            add(
                0,
                MockExamRecord(
                    id = completedAt,
                    score = score,
                    totalScore = totalScore,
                    passScore = passScore,
                    correctCount = correctCount,
                    wrongCount = wrongCount,
                    unansweredCount = unansweredCount,
                    questionCount = questionCount,
                    durationSeconds = durationSeconds,
                    completedAt = completedAt
                )
            )
            while (size > 50) removeAt(lastIndex)
        }
        val scores = readScores(subject).toMutableList().apply {
            add(0, score)
            while (size > 5) removeAt(lastIndex)
        }
        val previousPassCount = preferences.getInt("${prefix}_pass_count", 0)
        preferences.edit()
            .putInt("${prefix}_last_score", score)
            .putInt("${prefix}_total_score", totalScore)
            .putInt("${prefix}_correct_count", correctCount)
            .putInt("${prefix}_last_wrong_count", wrongCount)
            .putInt("${prefix}_question_count", questionCount)
            .putInt("${prefix}_duration_seconds", durationSeconds)
            .putLong("${prefix}_completed_at", completedAt)
            .putInt(
                "${prefix}_exam_count",
                preferences.getInt("${prefix}_exam_count", 0) + 1
            )
            .putInt(
                "${prefix}_pass_count",
                previousPassCount + if (score >= passScore) 1 else 0
            )
            .putString("${prefix}_recent_scores", scores.joinToString(","))
            .putString("${prefix}_records", encodeRecords(records))
            .apply()
    }

    fun getStatistics(subject: ExamSubject): MockExamStatistics {
        val prefix = subject.bankKey
        val scores = readScores(subject)
        val average = if (scores.isEmpty()) 0 else scores.average().toInt()
        return MockExamStatistics(
            examCount = preferences.getInt("${prefix}_exam_count", 0),
            passCount = preferences.getInt("${prefix}_pass_count", 0),
            recentAverageScore = average,
            lastWrongCount = preferences.getInt("${prefix}_last_wrong_count", 0),
            predictedPassRate = average.coerceIn(0, 100)
        )
    }

    private fun readScores(subject: ExamSubject): List<Int> =
        preferences.getString("${subject.bankKey}_recent_scores", null)
            .orEmpty()
            .split(",")
            .mapNotNull(String::toIntOrNull)

    fun getRecords(subject: ExamSubject): List<MockExamRecord> =
        preferences.getString("${subject.bankKey}_records", null)
            .orEmpty()
            .split(";")
            .mapNotNull(::decodeRecord)
            .sortedByDescending(MockExamRecord::completedAt)

    fun getRecord(subject: ExamSubject, recordId: Long): MockExamRecord? =
        getRecords(subject).firstOrNull { it.id == recordId }

    private fun encodeRecords(records: List<MockExamRecord>): String =
        records.joinToString(";") { record ->
            listOf(
                record.id,
                record.score,
                record.totalScore,
                record.passScore,
                record.correctCount,
                record.wrongCount,
                record.unansweredCount,
                record.questionCount,
                record.durationSeconds,
                record.completedAt
            ).joinToString("|")
        }

    private fun decodeRecord(value: String): MockExamRecord? {
        val parts = value.split("|")
        if (parts.size != 10) return null
        return MockExamRecord(
            id = parts[0].toLongOrNull() ?: return null,
            score = parts[1].toIntOrNull() ?: return null,
            totalScore = parts[2].toIntOrNull() ?: return null,
            passScore = parts[3].toIntOrNull() ?: return null,
            correctCount = parts[4].toIntOrNull() ?: return null,
            wrongCount = parts[5].toIntOrNull() ?: return null,
            unansweredCount = parts[6].toIntOrNull() ?: return null,
            questionCount = parts[7].toIntOrNull() ?: return null,
            durationSeconds = parts[8].toIntOrNull() ?: return null,
            completedAt = parts[9].toLongOrNull() ?: return null
        )
    }
}
