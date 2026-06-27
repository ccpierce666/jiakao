package cn.xmfengxing.kao.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import cn.xmfengxing.kao.R
import cn.xmfengxing.kao.data.ExamSubject
import cn.xmfengxing.kao.ui.screen.ExamScreen
import cn.xmfengxing.kao.ui.screen.MockExamMode
import cn.xmfengxing.kao.ui.screen.MockExamScreen
import cn.xmfengxing.kao.ui.screen.MockExamLobbyScreen
import cn.xmfengxing.kao.ui.screen.MockExamTranscriptScreen
import cn.xmfengxing.kao.ui.screen.PracticeQuestionMode
import cn.xmfengxing.kao.ui.screen.ProfileScreen
import cn.xmfengxing.kao.ui.screen.QuestionSearchScreen
import cn.xmfengxing.kao.ui.screen.SequentialQuestionScreen
import cn.xmfengxing.kao.ui.screen.SequencePracticeScreen
import cn.xmfengxing.kao.ui.screen.SkillTopicsScreen
import cn.xmfengxing.kao.ui.screen.SpecializedPracticeScreen
import cn.xmfengxing.kao.ui.screen.SpecializedPracticeType
import cn.xmfengxing.kao.ui.screen.VipPurchaseScreen
import cn.xmfengxing.kao.ui.screen.WrongFavoriteScreen

private const val SequencePracticeRoute = "sequence_practice/{subject}"
private const val QuestionPracticeRoute = "question_practice/{subject}/{mode}"
private const val VipPurchaseRoute = "vip_purchase"
private const val MockExamLobbyRoute = "mock_exam_lobby/{subject}"
private const val MockExamRoute = "mock_exam/{subject}"
private const val RealMockExamRoute = "real_mock_exam/{subject}"
private const val MockExamTranscriptRoute = "mock_exam_transcript/{subject}"
private const val SkillTopicsRoute = "skill_topics/{subject}"
private const val SkillTopicPracticeRoute = "skill_topic_practice/{subject}/{topicId}"
private const val SpecializedPracticeRoute = "specialized_practice/{subject}"
private const val SpecializedQuestionRoute =
    "specialized_questions/{subject}/{type}/{itemId}"
private const val WrongFavoriteRoute = "wrong_favorite/{subject}"
private const val QuestionSearchRoute = "question_search/{subject}"
private const val SearchQuestionPracticeRoute =
    "search_question_practice/{subject}/{questionId}"

private fun sequencePracticeRoute(subject: ExamSubject): String =
    "sequence_practice/${subject.routeValue}"

private fun mockExamRoute(subject: ExamSubject): String =
    "mock_exam/${subject.routeValue}"

private fun realMockExamRoute(subject: ExamSubject): String =
    "real_mock_exam/${subject.routeValue}"

private fun mockExamLobbyRoute(subject: ExamSubject): String =
    "mock_exam_lobby/${subject.routeValue}"

private fun mockExamTranscriptRoute(subject: ExamSubject): String =
    "mock_exam_transcript/${subject.routeValue}"

private fun skillTopicsRoute(subject: ExamSubject): String =
    "skill_topics/${subject.routeValue}"

private fun skillTopicPracticeRoute(subject: ExamSubject, topicId: Long): String =
    "skill_topic_practice/${subject.routeValue}/$topicId"

private fun specializedPracticeRoute(subject: ExamSubject): String =
    "specialized_practice/${subject.routeValue}"

private fun wrongFavoriteRoute(subject: ExamSubject): String =
    "wrong_favorite/${subject.routeValue}"

private fun questionSearchRoute(subject: ExamSubject): String =
    "question_search/${subject.routeValue}"

private fun searchQuestionPracticeRoute(
    subject: ExamSubject,
    questionId: Long
): String = "search_question_practice/${subject.routeValue}/$questionId"

private fun specializedQuestionRoute(
    subject: ExamSubject,
    type: SpecializedPracticeType,
    itemId: Long
): String = "specialized_questions/${subject.routeValue}/${type.routeValue}/$itemId"

private fun questionPracticeRoute(
    subject: ExamSubject,
    mode: PracticeQuestionMode
): String = "question_practice/${subject.routeValue}/${mode.routeValue}"

private enum class MainTab(
    val route: String,
    @StringRes val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    Exam(
        route = "exam",
        labelRes = R.string.tab_home,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    Profile(
        route = "profile",
        labelRes = R.string.tab_profile,
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val showBottomBar = currentDestination?.route !in setOf(
        SequencePracticeRoute,
        QuestionPracticeRoute,
        MockExamLobbyRoute,
        MockExamRoute,
        RealMockExamRoute,
        MockExamTranscriptRoute,
        SkillTopicsRoute,
        SkillTopicPracticeRoute,
        SpecializedPracticeRoute,
        SpecializedQuestionRoute,
        WrongFavoriteRoute,
        QuestionSearchRoute,
        SearchQuestionPracticeRoute,
        VipPurchaseRoute
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                Surface(
                    color = Color.White,
                    shadowElevation = 10.dp
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MainTab.entries.forEach { tab ->
                                val selected = currentDestination?.hierarchy?.any {
                                    it.route == tab.route
                                } == true

                                TabItem(
                                    tab = tab,
                                    selected = selected,
                                    onClick = {
                                        navController.navigate(tab.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                        Spacer(
                            modifier = Modifier.windowInsetsBottomHeight(
                                WindowInsets.navigationBars
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = MainTab.Exam.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(MainTab.Exam.route) {
                ExamScreen(
                    contentPadding = innerPadding,
                    onSearchClick = { subject ->
                        navController.navigate(questionSearchRoute(subject))
                    },
                    onSequencePracticeClick = { subject ->
                        navController.navigate(sequencePracticeRoute(subject))
                    },
                    onMockExamClick = { subject ->
                        navController.navigate(mockExamLobbyRoute(subject))
                    },
                    onRealMockExamClick = { subject ->
                        navController.navigate(realMockExamRoute(subject))
                    },
                    onSkillTipsClick = { subject ->
                        navController.navigate(skillTopicsRoute(subject))
                    },
                    onSpecializedPracticeClick = { subject ->
                        navController.navigate(specializedPracticeRoute(subject))
                    },
                    onWrongFavoriteClick = { subject ->
                        navController.navigate(wrongFavoriteRoute(subject))
                    },
                    onFeaturedQuestionsClick = { subject ->
                        navController.navigate(
                            questionPracticeRoute(subject, PracticeQuestionMode.Featured)
                        )
                    }
                )
            }
            composable(MainTab.Profile.route) { ProfileScreen(innerPadding) }
            composable(QuestionSearchRoute) { backStackEntry ->
                val subject = ExamSubject.fromRoute(
                    backStackEntry.arguments?.getString("subject")
                )
                QuestionSearchScreen(
                    subject = subject,
                    onBack = navController::navigateUp,
                    onQuestionClick = { questionId ->
                        navController.navigate(
                            searchQuestionPracticeRoute(subject, questionId)
                        )
                    }
                )
            }
            composable(SearchQuestionPracticeRoute) { backStackEntry ->
                SequentialQuestionScreen(
                    subject = ExamSubject.fromRoute(
                        backStackEntry.arguments?.getString("subject")
                    ),
                    directQuestionId = backStackEntry.arguments
                        ?.getString("questionId")
                        ?.toLongOrNull(),
                    directQuestionTitle = "搜索题目",
                    onVipClick = {
                        navController.navigate(VipPurchaseRoute)
                    },
                    onBack = navController::navigateUp
                )
            }
            composable(SequencePracticeRoute) { backStackEntry ->
                val subject = ExamSubject.fromRoute(
                    backStackEntry.arguments?.getString("subject")
                )
                SequencePracticeScreen(
                    subject = subject,
                    onBack = navController::navigateUp,
                    onAllQuestionsClick = {
                        navController.navigate(
                            questionPracticeRoute(subject, PracticeQuestionMode.All)
                        )
                    },
                    onUnpracticedQuestionsClick = {
                        navController.navigate(
                            questionPracticeRoute(subject, PracticeQuestionMode.Unpracticed)
                        )
                    },
                    onWrongQuestionsClick = {
                        navController.navigate(
                            questionPracticeRoute(subject, PracticeQuestionMode.Wrong)
                        )
                    },
                    onSelectedQuestionsClick = {
                        navController.navigate(
                            questionPracticeRoute(subject, PracticeQuestionMode.Featured)
                        )
                    }
                )
            }
            composable(QuestionPracticeRoute) { backStackEntry ->
                SequentialQuestionScreen(
                    subject = ExamSubject.fromRoute(
                        backStackEntry.arguments?.getString("subject")
                    ),
                    mode = PracticeQuestionMode.fromRoute(
                        backStackEntry.arguments?.getString("mode")
                    ),
                    onVipClick = {
                        navController.navigate(VipPurchaseRoute)
                    },
                    onBack = navController::navigateUp
                )
            }
            composable(VipPurchaseRoute) {
                VipPurchaseScreen(onBack = navController::navigateUp)
            }
            composable(MockExamRoute) { backStackEntry ->
                MockExamScreen(
                    subject = ExamSubject.fromRoute(
                        backStackEntry.arguments?.getString("subject")
                    ),
                    mode = MockExamMode.Standard,
                    onBack = navController::navigateUp
                )
            }
            composable(RealMockExamRoute) { backStackEntry ->
                MockExamScreen(
                    subject = ExamSubject.fromRoute(
                        backStackEntry.arguments?.getString("subject")
                    ),
                    mode = MockExamMode.Real,
                    onBack = navController::navigateUp
                )
            }
            composable(MockExamLobbyRoute) { backStackEntry ->
                val subject = ExamSubject.fromRoute(
                    backStackEntry.arguments?.getString("subject")
                )
                MockExamLobbyScreen(
                    subject = subject,
                    onBack = navController::navigateUp,
                    onStartExam = {
                        navController.navigate(mockExamRoute(subject))
                    },
                    onStartRealExam = {
                        navController.navigate(realMockExamRoute(subject))
                    },
                    onTranscriptClick = {
                        navController.navigate(mockExamTranscriptRoute(subject))
                    }
                )
            }
            composable(MockExamTranscriptRoute) { backStackEntry ->
                val subject = ExamSubject.fromRoute(
                    backStackEntry.arguments?.getString("subject")
                )
                MockExamTranscriptScreen(
                    subject = subject,
                    onBack = navController::navigateUp,
                    onStartExam = {
                        navController.navigate(mockExamRoute(subject))
                    }
                )
            }
            composable(SkillTopicsRoute) { backStackEntry ->
                val subject = ExamSubject.fromRoute(
                    backStackEntry.arguments?.getString("subject")
                )
                SkillTopicsScreen(
                    subject = subject,
                    onBack = navController::navigateUp,
                    onTopicClick = { topic ->
                        navController.navigate(skillTopicPracticeRoute(subject, topic.id))
                    },
                    onVipClick = {
                        navController.navigate(VipPurchaseRoute)
                    }
                )
            }
            composable(SkillTopicPracticeRoute) { backStackEntry ->
                SequentialQuestionScreen(
                    subject = ExamSubject.fromRoute(
                        backStackEntry.arguments?.getString("subject")
                    ),
                    skillTopicId = backStackEntry.arguments
                        ?.getString("topicId")
                        ?.toLongOrNull(),
                    skillTopicTitle = "答题技巧练习",
                    onVipClick = {
                        navController.navigate(VipPurchaseRoute)
                    },
                    onBack = navController::navigateUp
                )
            }
            composable(SpecializedPracticeRoute) { backStackEntry ->
                val subject = ExamSubject.fromRoute(
                    backStackEntry.arguments?.getString("subject")
                )
                SpecializedPracticeScreen(
                    subject = subject,
                    onBack = navController::navigateUp,
                    onItemClick = { type, item ->
                        navController.navigate(
                            specializedQuestionRoute(subject, type, item.id)
                        )
                    }
                )
            }
            composable(SpecializedQuestionRoute) { backStackEntry ->
                val type = SpecializedPracticeType.fromRoute(
                    backStackEntry.arguments?.getString("type")
                )
                SequentialQuestionScreen(
                    subject = ExamSubject.fromRoute(
                        backStackEntry.arguments?.getString("subject")
                    ),
                    specializedType = type,
                    specializedItemId = backStackEntry.arguments
                        ?.getString("itemId")
                        ?.toLongOrNull(),
                    specializedTitle = if (type == SpecializedPracticeType.Knowledge) {
                        "考点专项练习"
                    } else {
                        "章节专项练习"
                    },
                    onVipClick = {
                        navController.navigate(VipPurchaseRoute)
                    },
                    onBack = navController::navigateUp
                )
            }
            composable(WrongFavoriteRoute) { backStackEntry ->
                val subject = ExamSubject.fromRoute(
                    backStackEntry.arguments?.getString("subject")
                )
                WrongFavoriteScreen(
                    subject = subject,
                    onBack = navController::navigateUp,
                    onWrongPractice = {
                        navController.navigate(
                            questionPracticeRoute(subject, PracticeQuestionMode.Wrong)
                        )
                    },
                    onFavoritePractice = {
                        navController.navigate(
                            questionPracticeRoute(subject, PracticeQuestionMode.Favorite)
                        )
                    },
                    onVipClick = {
                        navController.navigate(VipPurchaseRoute)
                    }
                )
            }
        }
    }
}

@Composable
private fun RowScope.TabItem(
    tab: MainTab,
    selected: Boolean,
    onClick: () -> Unit
) {
    val label = stringResource(tab.labelRes)
    val color = if (selected) Color(0xFF2497F3) else Color(0xFF252525)
    Column(
        modifier = Modifier
            .width(60.dp)
            .fillMaxHeight()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(21.dp)
        )
        Spacer(Modifier.height(1.dp))
        Text(
            text = label,
            color = color,
            fontSize = 10.sp,
            lineHeight = 11.sp
        )
    }
}
