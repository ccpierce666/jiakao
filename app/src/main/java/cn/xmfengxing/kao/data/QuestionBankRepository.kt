package cn.xmfengxing.kao.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream

enum class ExamSubject(
    val kemu: Int,
    val routeValue: String,
    val title: String,
    val bankKey: String
) {
    SubjectOne(1, "subject_1", "科一", "small_car_subject_1_national"),
    SubjectFour(4, "subject_4", "科四", "small_car_subject_4_national");

    companion object {
        fun fromRoute(value: String?): ExamSubject =
            entries.firstOrNull { it.routeValue == value } ?: SubjectOne
    }
}

data class PracticeQuestion(
    val id: Long,
    val type: Int,
    val text: String,
    val options: List<String>,
    val correctAnswers: Set<Int>,
    val explanation: String,
    val knowledgePoint: String,
    val memoryTip: String,
    val answerTechnique: String,
    val difficulty: Int,
    val errorRate: Double,
    val imageBytes: ByteArray?
)

data class QuestionSearchResult(
    val id: Long,
    val type: Int,
    val text: String,
    val knowledgePoint: String
)

data class MockExamRule(
    val totalScore: Int,
    val passScore: Int,
    val durationSeconds: Int,
    val questionCount: Int,
    val judgeScore: Int,
    val singleScore: Int,
    val multiScore: Int,
    val judgeCount: Int,
    val singleCount: Int,
    val multiCount: Int
)

enum class SkillTopicCategory(val title: String) {
    All("全部"),
    JudgmentDetention("判断扣留"),
    Time("时间题"),
    PointsFine("扣分罚款"),
    Basic("基础知识")
}

data class SkillTopic(
    val id: Long,
    val title: String,
    val questionCount: Int,
    val category: SkillTopicCategory
)

data class SpecializedPracticeItem(
    val id: Long,
    val title: String,
    val questionCount: Int
)

class QuestionBankRepository(context: Context) {
    private val appContext = context.applicationContext

    fun searchQuestions(
        subject: ExamSubject,
        keyword: String,
        limit: Int = 100
    ): List<QuestionSearchResult> {
        val query = keyword.trim()
        if (query.isEmpty()) return emptyList()

        val likeValue = "%$query%"
        return openDatabase().use { database ->
            database.rawQuery(
                """
                SELECT DISTINCT
                    question.ID,
                    question.Type,
                    question.Question,
                    COALESCE(
                        (
                            SELECT NULLIF(TRIM(point.name), '')
                            FROM point_map point_mapping
                            JOIN Point point ON point.id = point_mapping.pid
                            WHERE point_mapping.qid = question.ID
                            ORDER BY point_mapping.id
                            LIMIT 1
                        ),
                        (
                            SELECT NULLIF(TRIM(third.name), '')
                            FROM PointThird_map third_mapping
                            JOIN PointThird third ON third.id = third_mapping.pid
                            WHERE third_mapping.qid = question.ID
                            ORDER BY third_mapping.sort_order, third_mapping.id
                            LIMIT 1
                        ),
                        ''
                    ) AS knowledge_point
                FROM t_city_question city_question
                JOIN web_note question
                  ON question.ID = city_question.question_id
                WHERE city_question.gs = 'xc'
                  AND city_question.kemu = ?
                  AND city_question.city_id = 0
                  AND question.gs <> 'test'
                  AND (
                        question.Question LIKE ?
                     OR question.An1 LIKE ?
                     OR question.An2 LIKE ?
                     OR question.An3 LIKE ?
                     OR question.An4 LIKE ?
                     OR question.An5 LIKE ?
                     OR question.An6 LIKE ?
                     OR question.An7 LIKE ?
                     OR EXISTS (
                            SELECT 1
                            FROM point_map point_mapping
                            JOIN Point point ON point.id = point_mapping.pid
                            WHERE point_mapping.qid = question.ID
                              AND point.name LIKE ?
                        )
                     OR EXISTS (
                            SELECT 1
                            FROM PointThird_map third_mapping
                            JOIN PointThird third ON third.id = third_mapping.pid
                            WHERE third_mapping.qid = question.ID
                              AND third.name LIKE ?
                        )
                  )
                ORDER BY question.ID
                LIMIT ?
                """.trimIndent(),
                arrayOf(
                    subject.kemu.toString(),
                    likeValue,
                    likeValue,
                    likeValue,
                    likeValue,
                    likeValue,
                    likeValue,
                    likeValue,
                    likeValue,
                    likeValue,
                    likeValue,
                    limit.coerceIn(1, 200).toString()
                )
            ).use { cursor ->
                buildList {
                    while (cursor.moveToNext()) {
                        add(
                            QuestionSearchResult(
                                id = cursor.getLong(0),
                                type = cursor.getInt(1),
                                text = cursor.getString(2).orEmpty(),
                                knowledgePoint = cursor.getString(3).orEmpty()
                            )
                        )
                    }
                }
            }
        }
    }

    fun loadSmallCarQuestionIds(subject: ExamSubject): List<Long> {
        return openDatabase().use { database ->
            database.rawQuery(
                """
                SELECT question.ID
                FROM t_city_question mapping
                JOIN web_note question ON question.ID = mapping.question_id
                WHERE mapping.gs = 'xc'
                  AND mapping.kemu = ?
                  AND mapping.city_id = 0
                  AND question.gs <> 'test'
                ORDER BY question.ID
                """.trimIndent(),
                arrayOf(subject.kemu.toString())
            ).use { cursor ->
                buildList {
                    while (cursor.moveToNext()) {
                        add(cursor.getLong(0))
                    }
                }
            }
        }
    }

    fun loadFeaturedQuestionIds(subject: ExamSubject): List<Long> {
        return openDatabase().use { database ->
            database.rawQuery(
                """
                SELECT DISTINCT category.question_id
                FROM t_app_question_category category
                JOIN t_city_question city_question
                  ON city_question.question_id = category.question_id
                 AND city_question.gs = 'xc'
                 AND city_question.kemu = ?
                 AND city_question.city_id = 0
                WHERE category.gs = 'xc'
                  AND category.kemu = ?
                  AND category.city_id = 0
                  AND category.category = 5
                ORDER BY category.sort_order, category.question_id
                """.trimIndent(),
                arrayOf(subject.kemu.toString(), subject.kemu.toString())
            ).use { cursor ->
                buildList {
                    while (cursor.moveToNext()) add(cursor.getLong(0))
                }
            }
        }
    }

    fun loadMockExamRule(subject: ExamSubject): MockExamRule {
        return openDatabase().use { database ->
            database.rawQuery(
                """
                SELECT totalScore, passScore, time, questionCount,
                       judgeScore, singleScore, multiScore,
                       judgeSumCount, singleSumCount, multiSumCount
                FROM t_app_exam_regular
                WHERE gs = 'xc' AND kemu = ? AND cityId = 0
                LIMIT 1
                """.trimIndent(),
                arrayOf(subject.kemu.toString())
            ).use { cursor ->
                check(cursor.moveToFirst()) { "未找到${subject.title}考试规则" }
                MockExamRule(
                    totalScore = cursor.getInt(0),
                    passScore = cursor.getInt(1),
                    durationSeconds = cursor.getInt(2),
                    questionCount = cursor.getInt(3),
                    judgeScore = cursor.getDouble(4).toInt(),
                    singleScore = cursor.getDouble(5).toInt(),
                    multiScore = cursor.getDouble(6).toInt(),
                    judgeCount = cursor.getInt(7),
                    singleCount = cursor.getInt(8),
                    multiCount = cursor.getInt(9)
                )
            }
        }
    }

    fun createMockExamQuestionIds(subject: ExamSubject, rule: MockExamRule): List<Long> {
        return openDatabase().use { database ->
            val idsByType = mutableMapOf<Int, MutableList<Long>>()
            database.rawQuery(
                """
                SELECT question.ID, question.Type
                FROM t_city_question mapping
                JOIN web_note question ON question.ID = mapping.question_id
                WHERE mapping.gs = 'xc'
                  AND mapping.kemu = ?
                  AND mapping.city_id = 0
                  AND question.gs <> 'test'
                """.trimIndent(),
                arrayOf(subject.kemu.toString())
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    idsByType.getOrPut(cursor.getInt(1)) { mutableListOf() }
                        .add(cursor.getLong(0))
                }
            }
            buildList {
                addAll(idsByType[1].orEmpty().shuffled().take(rule.judgeCount))
                addAll(idsByType[2].orEmpty().shuffled().take(rule.singleCount))
                addAll(idsByType[3].orEmpty().shuffled().take(rule.multiCount))
            }.shuffled()
        }
    }

    fun loadSkillTopics(subject: ExamSubject): List<SkillTopic> {
        return openDatabase().use { database ->
            database.rawQuery(
                """
                SELECT third.id, third.name, COUNT(DISTINCT mapping.qid) AS question_count
                FROM PointThird third
                JOIN PointThird_map mapping ON mapping.pid = third.id
                JOIN t_city_question city_question
                  ON city_question.question_id = mapping.qid
                 AND city_question.gs = 'xc'
                 AND city_question.kemu = ?
                 AND city_question.city_id = 0
                GROUP BY third.id, third.name
                HAVING question_count >= 3
                ORDER BY question_count DESC, third.frequency DESC, third.id
                """.trimIndent(),
                arrayOf(subject.kemu.toString())
            ).use { cursor ->
                buildList {
                    while (cursor.moveToNext()) {
                        val title = cursor.getString(1).orEmpty()
                        add(
                            SkillTopic(
                                id = cursor.getLong(0),
                                title = title,
                                questionCount = cursor.getInt(2),
                                category = classifySkillTopic(title)
                            )
                        )
                    }
                }
            }
        }
    }

    fun loadSkillTopicQuestionIds(subject: ExamSubject, topicId: Long): List<Long> {
        return openDatabase().use { database ->
            database.rawQuery(
                """
                SELECT DISTINCT question.ID
                FROM PointThird_map mapping
                JOIN web_note question ON question.ID = mapping.qid
                JOIN t_city_question city_question
                  ON city_question.question_id = question.ID
                 AND city_question.gs = 'xc'
                 AND city_question.kemu = ?
                 AND city_question.city_id = 0
                WHERE mapping.pid = ?
                ORDER BY mapping.sort_order, question.ID
                """.trimIndent(),
                arrayOf(subject.kemu.toString(), topicId.toString())
            ).use { cursor ->
                buildList {
                    while (cursor.moveToNext()) add(cursor.getLong(0))
                }
            }
        }
    }

    fun loadSpecializedKnowledgePoints(
        subject: ExamSubject
    ): List<SpecializedPracticeItem> {
        return openDatabase().use { database ->
            database.rawQuery(
                """
                SELECT point.id, point.name, COUNT(DISTINCT mapping.qid)
                FROM Point point
                JOIN point_map mapping ON mapping.pid = point.id
                JOIN t_city_question city_question
                  ON city_question.question_id = mapping.qid
                 AND city_question.gs = 'xc'
                 AND city_question.kemu = ?
                 AND city_question.city_id = 0
                WHERE point.kemu = ?
                  AND point.gs LIKE '%xc%'
                GROUP BY point.id, point.name
                ORDER BY point.id
                """.trimIndent(),
                arrayOf(subject.kemu.toString(), subject.kemu.toString())
            ).use { cursor ->
                buildList {
                    while (cursor.moveToNext()) {
                        add(
                            SpecializedPracticeItem(
                                id = cursor.getLong(0),
                                title = cursor.getString(1).orEmpty(),
                                questionCount = cursor.getInt(2)
                            )
                        )
                    }
                }
            }
        }
    }

    fun loadSpecializedStages(subject: ExamSubject): List<SpecializedPracticeItem> {
        return openDatabase().use { database ->
            database.rawQuery(
                """
                SELECT stage.id, stage.name, COUNT(DISTINCT mapping.qid)
                FROM PointStage stage
                JOIN Point point ON point.pid = stage.id
                JOIN point_map mapping ON mapping.pid = point.id
                JOIN t_city_question city_question
                  ON city_question.question_id = mapping.qid
                 AND city_question.gs = 'xc'
                 AND city_question.kemu = ?
                 AND city_question.city_id = 0
                WHERE stage.kemu = ?
                  AND stage.gs LIKE '%xc%'
                  AND stage.name NOT LIKE '%河南%'
                GROUP BY stage.id, stage.name
                HAVING COUNT(DISTINCT mapping.qid) > 0
                ORDER BY stage.id
                """.trimIndent(),
                arrayOf(subject.kemu.toString(), subject.kemu.toString())
            ).use { cursor ->
                buildList {
                    while (cursor.moveToNext()) {
                        add(
                            SpecializedPracticeItem(
                                id = cursor.getLong(0),
                                title = cursor.getString(1).orEmpty(),
                                questionCount = cursor.getInt(2)
                            )
                        )
                    }
                }
            }
        }
    }

    fun loadKnowledgePointQuestionIds(
        subject: ExamSubject,
        pointId: Long
    ): List<Long> = loadMappedQuestionIds(
        subject = subject,
        sql = """
            SELECT DISTINCT question.ID
            FROM point_map mapping
            JOIN web_note question ON question.ID = mapping.qid
            JOIN t_city_question city_question
              ON city_question.question_id = question.ID
             AND city_question.gs = 'xc'
             AND city_question.kemu = ?
             AND city_question.city_id = 0
            WHERE mapping.pid = ?
            ORDER BY question.ID
        """.trimIndent(),
        id = pointId
    )

    fun loadStageQuestionIds(
        subject: ExamSubject,
        stageId: Long
    ): List<Long> = loadMappedQuestionIds(
        subject = subject,
        sql = """
            SELECT DISTINCT question.ID
            FROM Point point
            JOIN point_map mapping ON mapping.pid = point.id
            JOIN web_note question ON question.ID = mapping.qid
            JOIN t_city_question city_question
              ON city_question.question_id = question.ID
             AND city_question.gs = 'xc'
             AND city_question.kemu = ?
             AND city_question.city_id = 0
            WHERE point.pid = ?
            ORDER BY question.ID
        """.trimIndent(),
        id = stageId
    )

    fun loadQuestion(id: Long): PracticeQuestion? {
        return openDatabase().use { database ->
            database.rawQuery(
                """
                SELECT
                    q.ID,
                    q.Type,
                    q.Question,
                    q.An1,
                    q.An2,
                    q.An3,
                    q.An4,
                    q.An5,
                    q.An6,
                    q.An7,
                    q.AnswerTrue,
                    COALESCE(
                        NULLIF(TRIM(q.best_explain_new), ''),
                        NULLIF(TRIM(q.explain), ''),
                        (
                            SELECT COALESCE(
                                NULLIF(TRIM(point.red_content), ''),
                                NULLIF(TRIM(point.content), '')
                            )
                            FROM point_map mapping
                            JOIN Point point ON point.id = mapping.pid
                            WHERE mapping.qid = q.ID
                            ORDER BY mapping.id
                            LIMIT 1
                        ),
                        ''
                    ),
                    COALESCE(
                        (
                            SELECT NULLIF(TRIM(point.name), '')
                            FROM point_map mapping
                            JOIN Point point ON point.id = mapping.pid
                            WHERE mapping.qid = q.ID
                            ORDER BY mapping.id
                            LIMIT 1
                        ),
                        (
                            SELECT NULLIF(TRIM(third.name), '')
                            FROM PointThird_map mapping
                            JOIN PointThird third ON third.id = mapping.pid
                            WHERE mapping.qid = q.ID
                            ORDER BY mapping.sort_order
                            LIMIT 1
                        ),
                        ''
                    ),
                    COALESCE(
                        (
                            SELECT NULLIF(TRIM(third.name), '')
                            FROM PointThird_map mapping
                            JOIN PointThird third ON third.id = mapping.pid
                            WHERE mapping.qid = q.ID
                            ORDER BY mapping.sort_order, mapping.id
                            LIMIT 1
                        ),
                        ''
                    ),
                    COALESCE(
                        (
                            SELECT NULLIF(TRIM(blackboard.voiceSubtitles), '')
                            FROM t_app_question_black_board blackboard
                            WHERE blackboard.question_id = q.ID
                            ORDER BY blackboard.id
                            LIMIT 1
                        ),
                        ''
                    ),
                    COALESCE(q.diff_degree, 0),
                    COALESCE(q.error_rate, 0),
                    (
                        SELECT media_content
                        FROM t_app_question_media media
                        WHERE media.webp_url = q.media_url
                           OR media.webp_url = q.sinaimg
                           OR media.media_name = q.question_media_name
                        LIMIT 1
                    )
                FROM web_note q
                WHERE q.ID = ?
                LIMIT 1
                """.trimIndent(),
                arrayOf(id.toString())
            ).use { cursor ->
                if (!cursor.moveToFirst()) {
                    return@use null
                }

                val type = cursor.getInt(1)
                val options = if (type == 1) {
                    listOf("正确", "错误")
                } else {
                    (3..9).mapNotNull { column ->
                        cursor.getString(column)?.trim()?.takeIf(String::isNotEmpty)
                    }
                }
                val answers = cursor.getString(10)
                    .orEmpty()
                    .mapNotNull { character ->
                        character.digitToIntOrNull()?.takeIf { it in 1..options.size }
                    }
                    .toSet()

                PracticeQuestion(
                    id = cursor.getLong(0),
                    type = type,
                    text = cursor.getString(2).orEmpty(),
                    options = options,
                    correctAnswers = answers,
                    explanation = cursor.getString(11).orEmpty(),
                    knowledgePoint = cursor.getString(12).orEmpty(),
                    memoryTip = cursor.getString(13).orEmpty(),
                    answerTechnique = cursor.getString(14).orEmpty(),
                    difficulty = cursor.getInt(15),
                    errorRate = cursor.getDouble(16),
                    imageBytes = if (cursor.isNull(17)) null else cursor.getBlob(17)
                )
            }
        }
    }

    private fun openDatabase(): SQLiteDatabase {
        val databaseFile = ensureDatabaseCopied()
        return SQLiteDatabase.openDatabase(
            databaseFile.absolutePath,
            null,
            SQLiteDatabase.OPEN_READONLY
        )
    }

    private fun loadMappedQuestionIds(
        subject: ExamSubject,
        sql: String,
        id: Long
    ): List<Long> {
        return openDatabase().use { database ->
            database.rawQuery(
                sql,
                arrayOf(subject.kemu.toString(), id.toString())
            ).use { cursor ->
                buildList {
                    while (cursor.moveToNext()) add(cursor.getLong(0))
                }
            }
        }
    }

    private fun ensureDatabaseCopied(): File {
        val databaseDirectory = File(appContext.filesDir, "question_banks")
        val databaseFile = File(databaseDirectory, DATABASE_NAME)
        if (isValidDatabaseFile(databaseFile)) {
            return databaseFile
        }

        return synchronized(DATABASE_INSTALL_LOCK) {
            if (isValidDatabaseFile(databaseFile)) {
                return@synchronized databaseFile
            }

            check(databaseDirectory.exists() || databaseDirectory.mkdirs()) {
                "Unable to create local question bank directory"
            }

            if (databaseFile.exists()) {
                check(databaseFile.delete()) {
                    "Unable to remove incomplete local question bank"
                }
            }

            val temporaryFile = File.createTempFile(
                "$DATABASE_NAME.",
                ".tmp",
                databaseDirectory
            )
            try {
                val copiedBytes = copyAssetToFile(temporaryFile)
                check(copiedBytes == ASSET_DATABASE_SIZE) {
                    "Incomplete local question bank copy: $copiedBytes/$ASSET_DATABASE_SIZE"
                }

                if (!temporaryFile.renameTo(databaseFile)) {
                    copyFileWithSync(temporaryFile, databaseFile)
                }

                check(isValidDatabaseFile(databaseFile)) {
                    "Installed local question bank is invalid"
                }
                databaseFile
            } finally {
                temporaryFile.delete()
            }
        }
    }

    private fun copyAssetToFile(destination: File): Long {
        return appContext.assets.open(ASSET_PATH).use { input ->
            FileOutputStream(destination).use { fileOutput ->
                BufferedOutputStream(fileOutput).use { output ->
                    val copiedBytes = input.copyTo(output)
                    output.flush()
                    fileOutput.fd.sync()
                    copiedBytes
                }
            }
        }
    }

    private fun copyFileWithSync(source: File, destination: File) {
        source.inputStream().use { input ->
            FileOutputStream(destination).use { fileOutput ->
                BufferedOutputStream(fileOutput).use { output ->
                    input.copyTo(output)
                    output.flush()
                    fileOutput.fd.sync()
                }
            }
        }
    }

    private fun isValidDatabaseFile(file: File): Boolean {
        if (!file.isFile || file.length() != ASSET_DATABASE_SIZE) return false
        return runCatching {
            val header = ByteArray(SQLITE_HEADER.size)
            file.inputStream().use { input ->
                input.read(header) == header.size
            } && header.contentEquals(SQLITE_HEADER)
        }.getOrDefault(false)
    }

    private companion object {
        const val DATABASE_NAME = "updb.db"
        const val ASSET_PATH = "databases/updb.db"
        const val ASSET_DATABASE_SIZE = 60_760_064L
        val SQLITE_HEADER = "SQLite format 3\u0000".toByteArray(Charsets.US_ASCII)
        val DATABASE_INSTALL_LOCK = Any()
    }
}

private fun classifySkillTopic(title: String): SkillTopicCategory {
    return when {
        listOf("扣车", "扣留", "暂扣", "吊销", "收缴").any(title::contains) ->
            SkillTopicCategory.JudgmentDetention

        listOf(
            "时间", "期限", "终生", "周岁", "年内", "个月", "小时",
            "分钟", "每日", "每年", "天内"
        ).any(title::contains) ->
            SkillTopicCategory.Time

        listOf(
            "扣分", "记分", "罚款", "处罚", "12分", "9分", "6分",
            "3分", "1分", "徒刑", "拘役", "犯罪"
        ).any(title::contains) ->
            SkillTopicCategory.PointsFine

        else -> SkillTopicCategory.Basic
    }
}
