package com.app.presentation.ui.Screen.Main.Badge

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.app.presentation.R
import com.app.presentation.ui.event.UiEvent
import com.app.presentation.ui.theme.MainBlue
import com.app.presentation.viewModel.BadgeViewModel
import com.patrykandpatrick.vico.compose.common.component.shadow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgeScreen(
    navController: NavHostController,
    viewModel: BadgeViewModel = hiltViewModel()
) {
    val badges by viewModel.badges.collectAsState()
    var dialogBadgeKey by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val badgeList = listOf(
        "day_2L",
        "week_7days",
        "month_30days",
        "king_6months"
    )

    // 이벤트 수신
    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is UiEvent.ShowBadgeDialog -> dialogBadgeKey = event.badgeKey
                is UiEvent.ShowToast -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                is UiEvent.NavigateToLogin -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("물뱃지") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "뒤로가기 아이콘",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MainBlue,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                )
            )
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.padding(innerPadding)
        ) {
            items(badgeList) { key ->
                val badge = badges[key]

                BadgeItem(
                    isAchieved = badge != null,
                    title = badge?.name ?: "🔒잠김"
                )
            }
        }
    }

    // 다이얼로그 표시
    dialogBadgeKey?.let { key ->
        BadgeEarnedDialog(
            badgeKey = key,
            badgeName = badges[key]?.name ?: "",
            onDismiss = { dialogBadgeKey = null }
        )
    }
}

@Composable
fun BadgeItem(
    isAchieved: Boolean,
    title: String
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            val imageRes = if (isAchieved) {
                R.drawable.badge_active
            } else {
                R.drawable.badge_inactive
            }

            Image(
                painter = painterResource(imageRes),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .aspectRatio(1f)
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isAchieved) Color(0xFF2979FF) else Color.Gray
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (isAchieved) {
                Text(
                    text = "획득완료",
                    color = Color(0xFF4CAF50),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Text(
                    text = "뱃지를 획득해보세요",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}