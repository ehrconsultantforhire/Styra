package com.example.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.CalendarContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.models.ClothingItem
import com.example.data.models.StyleProfile
import com.example.data.models.SavedOutfit
import com.example.data.models.CalendarEvent
import com.example.ui.theme.*
import androidx.compose.foundation.border


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StyraApp(viewModel: FashionViewModel) {
    var currentTab by remember { mutableStateOf("home") }

    val context = LocalContext.current
    val clothes by viewModel.clothingItems.collectAsStateWithLifecycle()
    val events by viewModel.calendarEvents.collectAsStateWithLifecycle()
    val styleProfile by viewModel.styleProfile.collectAsStateWithLifecycle()
    val savedOutfits by viewModel.savedOutfits.collectAsStateWithLifecycle()

    val currentEvent by viewModel.currentEvent.collectAsStateWithLifecycle()
    val temperature by viewModel.temperature.collectAsStateWithLifecycle()
    val weatherCondition by viewModel.weatherCondition.collectAsStateWithLifecycle()
    val humidity by viewModel.humidity.collectAsStateWithLifecycle()
    val windSpeed by viewModel.windSpeed.collectAsStateWithLifecycle()
    val uvIndex by viewModel.uvIndex.collectAsStateWithLifecycle()

    val proposedTitle by viewModel.proposedOutfitTitle.collectAsStateWithLifecycle()
    val proposedCommentary by viewModel.proposedCommentary.collectAsStateWithLifecycle()

    val pTop by viewModel.proposedTop.collectAsStateWithLifecycle()
    val pBottom by viewModel.proposedBottom.collectAsStateWithLifecycle()
    val pShoes by viewModel.proposedShoes.collectAsStateWithLifecycle()
    val pJacket by viewModel.proposedJacket.collectAsStateWithLifecycle()
    val pAccessory by viewModel.proposedAccessory.collectAsStateWithLifecycle()

    val isGeneratingOutfit by viewModel.isGeneratingOutfit.collectAsStateWithLifecycle()

    // Dialog state for adding manual/AI items
    var showAddItemDialog by remember { mutableStateOf(false) }

    // Dialog state for weather selector
    var showWeatherDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BoneWhite,
        bottomBar = {
            StyleBottomNav(
                currentTab = currentTab,
                onTabSelect = { currentTab = it },
                onAddClick = { showAddItemDialog = true }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentTab) {
                "home" -> HomeScreen(
                    currentEvent = currentEvent,
                    temperature = temperature,
                    weatherCondition = weatherCondition,
                    humidity = humidity,
                    windSpeed = windSpeed,
                    proposedTitle = proposedTitle,
                    proposedCommentary = proposedCommentary,
                    pTop = pTop,
                    pBottom = pBottom,
                    pShoes = pShoes,
                    pJacket = pJacket,
                    pAccessory = pAccessory,
                    isGenerating = isGeneratingOutfit,
                    onRegenerate = { viewModel.regenerateOutfitProposal() },
                    onWearClick = {
                        Toast.makeText(context, "Log active: Outfit set to 'Wearing' status and logged!", Toast.LENGTH_SHORT).show()
                    },
                    onBookmarkClick = {
                        viewModel.saveProposedAsFavorite()
                        Toast.makeText(context, "Added to saved styles!", Toast.LENGTH_SHORT).show()
                    },
                    onWeatherBarClick = { showWeatherDialog = true },
                    onShareClick = {
                        shareOutfitToSocials(
                            context = context,
                            title = proposedTitle,
                            top = pTop?.name ?: "No Top Match",
                            bottom = pBottom?.name ?: "No Bottom Match",
                            shoes = pShoes?.name ?: "No Shoes Match",
                            commentary = proposedCommentary
                        )
                    }
                )
                "closet" -> {
                    val isAnalyzing by viewModel.isAnalyzingWardrobe.collectAsStateWithLifecycle()
                    val wardrobeAnalysis by viewModel.wardrobeAnalysis.collectAsStateWithLifecycle()
                    ClosetScreen(
                        clothes = clothes,
                        isAnalyzing = isAnalyzing,
                        analysis = wardrobeAnalysis,
                        onTriggerAnalysis = { viewModel.runWardrobeAnalysis() },
                        onStatusChange = { item, stat -> viewModel.changeLaundryStatus(item, stat) },
                        onDelete = { viewModel.deleteClothingItem(it) },
                        onAddItem = { name, type, color, season, formality, pattern, brand, condition, img ->
                            viewModel.addClothingItem(name, type, color, season, formality, pattern, brand, condition, img)
                        }
                    )
                }
                "quiz" -> QuizScreen(
                    profile = styleProfile,
                    onSave = {
                        viewModel.updateStyleProfile(it)
                        Toast.makeText(context, "Your Style profile updated seamlessly! Recommended outfits changed.", Toast.LENGTH_LONG).show()
                    }
                )
                "calendar" -> CalendarScreen(
                    events = events,
                    onAddEvent = { title, cat, comment, date, time ->
                        viewModel.addCalendarEvent(title, cat, comment, date, time)
                        viewModel.changeEventSim(title)
                        Toast.makeText(context, "Added event: Recommendations updated!", Toast.LENGTH_SHORT).show()
                    },
                    onDeleteEvent = { viewModel.removeCalendarEvent(it) },
                    onSelectEvent = {
                        viewModel.changeEventSim(it.title)
                        Toast.makeText(context, "Simulating: '${it.title}' (Category ${it.eventType}) active!", Toast.LENGTH_SHORT).show()
                    },
                    currentSimulatedTitle = currentEvent
                )
                "profile" -> ProfileScreen(
                    savedOutfits = savedOutfits,
                    clothes = clothes,
                    userEmail = "carl.anthony.osborne@gmail.com",
                    onDeleteSaved = { viewModel.deleteSavedOutfit(it) }
                )
            }

            // Weather Simulator Dialog
            if (showWeatherDialog) {
                WeatherSimulatorDialog(
                    currentTemp = temperature,
                    currentCond = weatherCondition,
                    currentHum = humidity,
                    currentWind = windSpeed,
                    currentUv = uvIndex,
                    onDismiss = { showWeatherDialog = false },
                    onApply = { temp, cond, hum, wind, uv ->
                        viewModel.changeWeatherSim(temp, cond, hum, wind, uv)
                        showWeatherDialog = false
                        Toast.makeText(context, "Weather simulator shifted to $temp $cond!", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // AI Photo Addition / Tagging Dialog
            if (showAddItemDialog) {
                AddItemDialog(
                    viewModel = viewModel,
                    onDismiss = {
                        viewModel.clearAiTagging()
                        showAddItemDialog = false
                    },
                    onItemAdded = { name, type, color, season, formality, pattern, brand, condition, imageUri ->
                        viewModel.addClothingItem(name, type, color, season, formality, pattern, brand, condition, imageUri)
                        showAddItemDialog = false
                        Toast.makeText(context, "Added $name successfully to Styra!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

// --- Dynamic Styled Components ---

@Composable
fun StyleBottomNav(
    currentTab: String,
    onTabSelect: (String) -> Unit,
    onAddClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = BoneWhite,
        border = BorderStroke(0.5.dp, StyraBorderOutline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            // Home Tab
            BottomNavItem(
                icon = Icons.Outlined.Home,
                activeIcon = Icons.Filled.Home,
                label = "HOME",
                isActive = currentTab == "home",
                onClick = { onTabSelect("home") },
                testTag = "home_tab"
            )

            // Closet Tab
            BottomNavItem(
                icon = Icons.Outlined.Checkroom,
                activeIcon = Icons.Filled.Checkroom,
                label = "CLOSET",
                isActive = currentTab == "closet",
                onClick = { onTabSelect("closet") },
                testTag = "closet_tab"
            )

            // Center Camera Add Trigger
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(StyraDark)
                    .clickable { onAddClick() }
                    .testTag("add_photo_fab"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = "Add Clothes with AI",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Calendar Tab
            BottomNavItem(
                icon = Icons.Outlined.CalendarMonth,
                activeIcon = Icons.Filled.CalendarMonth,
                label = "AGENDA",
                isActive = currentTab == "calendar",
                onClick = { onTabSelect("calendar") },
                testTag = "calendar_tab"
            )

            // Profile Tab
            BottomNavItem(
                icon = Icons.Outlined.Person,
                activeIcon = Icons.Filled.Person,
                label = "PROFILE",
                isActive = currentTab == "profile",
                onClick = { onTabSelect("profile") },
                testTag = "profile_tab"
            )
        }
    }
}

@Composable
fun BottomNavItem(
    icon: ImageVector,
    activeIcon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        modifier = Modifier
            .width(64.dp)
            .clickable { onClick() }
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isActive) activeIcon else icon,
            contentDescription = label,
            tint = if (isActive) StyraDark else GraySecondary.copy(alpha = 0.7f),
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 8.5.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
            color = if (isActive) StyraDark else GraySecondary.copy(alpha = 0.7f),
            letterSpacing = 1.sp
        )
    }
}

// --- Home Screen Layout ---

@Composable
fun HomeScreen(
    currentEvent: String,
    temperature: String,
    weatherCondition: String,
    humidity: String,
    windSpeed: String,
    proposedTitle: String,
    proposedCommentary: String,
    pTop: ClothingItem?,
    pBottom: ClothingItem?,
    pShoes: ClothingItem?,
    pJacket: ClothingItem?,
    pAccessory: ClothingItem?,
    isGenerating: Boolean,
    onRegenerate: () -> Unit,
    onWearClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onWeatherBarClick: () -> Unit,
    onShareClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "S T Y R A",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Black,
                        color = StyraDark,
                        letterSpacing = 3.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(OrangeLive)
                        )
                        Text(
                            text = "Matching $currentEvent",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GraySecondary
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(OlivePale)
                        .clickable { onRegenerate() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "A",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = StyraDark
                    )
                }
            }
        }

        // Weather card
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(GreenSlateBar)
                    .clickable { onWeatherBarClick() }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when {
                                weatherCondition.lowercase().contains("cloud") -> "⛅"
                                weatherCondition.lowercase().contains("rain") -> "🌧️"
                                weatherCondition.lowercase().contains("snow") -> "❄️"
                                weatherCondition.lowercase().contains("storm") -> "⛈️"
                                else -> "☀️"
                            },
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "$temperature / $weatherCondition",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = StyraDark
                        )
                        Text(
                            text = "Humidity $humidity • Wind $windSpeed",
                            fontSize = 11.sp,
                            color = StyraDark.copy(alpha = 0.7f)
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Simulate",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = OliveDeep,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Simulate values",
                        tint = StyraDark,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Main Outfit Box
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                border = BorderStroke(1.dp, OliveBorder.copy(alpha = 0.4f)),
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    // Title section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .background(OlivePale, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "OOTD STYLE REC",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OliveDeep,
                                    letterSpacing = 1.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = proposedTitle,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = StyraDark
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = onBookmarkClick,
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.White, CircleShape)
                                    .border(1.dp, Color.LightGray.copy(alpha = 0.6f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Save outfit",
                                    tint = YellowStar,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = onShareClick,
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.White, CircleShape)
                                    .border(1.dp, Color.LightGray.copy(alpha = 0.6f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share outfit",
                                    tint = OliveDeep,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isGenerating) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = OliveDeep)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                "Styra is analyzing weather & styles...",
                                fontSize = 12.sp,
                                color = GraySecondary
                            )
                        }
                    } else {
                        // Display Grid of recommending pieces
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutfitMatchSquare(
                                label = "Top Wear",
                                symbol = "👕",
                                item = pTop,
                                modifier = Modifier.weight(1f)
                            )
                            OutfitMatchSquare(
                                label = "Bottom Wear",
                                symbol = "👖",
                                item = pBottom,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutfitMatchSquare(
                                label = "Footwear",
                                symbol = "👞",
                                item = pShoes,
                                modifier = Modifier.weight(1f)
                            )
                            OutfitMatchSquare(
                                label = if (pJacket != null) "Layer" else "Accessory",
                                symbol = if (pJacket != null) "🧥" else "🕶️",
                                item = pJacket ?: pAccessory,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Commentary Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(CreamLightContainer)
                                .border(BorderStroke(1.dp, OliveBorder.copy(alpha = 0.4f)), RoundedCornerShape(16.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ElectricBolt,
                                        contentDescription = "Smart insights",
                                        tint = OliveDeep,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "STYRA AI INSIGHT",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OliveDeep
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = proposedCommentary,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = StyraDark
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = onWearClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("log_wearing_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OliveDeep,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "LOG AS WEARING TODAY",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }

        // Quick Insights widgets
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Widget 1
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(CreamLightContainer)
                        .border(1.dp, OliveBorder.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(OlivePale),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📅", fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "Agenda Ready",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = GraySecondary
                            )
                            Text(
                                "In Sync",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = StyraDark
                            )
                        }
                    }
                }

                // Widget 2
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(CreamLightContainer)
                        .border(1.dp, OliveBorder.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(OlivePale),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✨", fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "Style Match",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = GraySecondary
                            )
                            Text(
                                "98% Match",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = StyraDark
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OutfitMatchSquare(
    label: String,
    symbol: String,
    item: ClothingItem?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxHeight(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(0.5.dp, StyraBorderOutline),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label.uppercase(),
                fontSize = 8.sp,
                color = GraySecondary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(CreamLightContainer),
                contentAlignment = Alignment.Center
            ) {
                if (item != null && item.imageUri.isNotEmpty() && !item.imageUri.startsWith("default_")) {
                    AsyncImage(
                        model = item.imageUri,
                        contentDescription = item.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(text = symbol, fontSize = 22.sp)
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = item?.name ?: "NO WEARABLE",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (item != null) StyraDark else OrangeLive,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
                Text(
                    text = item?.brand?.uppercase() ?: "RESERVED",
                    fontSize = 7.5.sp,
                    color = GraySecondary,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun ClosetScreen(
    clothes: List<ClothingItem>,
    isAnalyzing: Boolean,
    analysis: WardrobeAnalysisResult?,
    onTriggerAnalysis: () -> Unit,
    onStatusChange: (ClothingItem, String) -> Unit,
    onDelete: (ClothingItem) -> Unit,
    onAddItem: (String, String, String, String, String, String, String, String, String) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("All") }
    val categories = listOf("All", "Top", "Bottom", "Shoes", "Jacket", "Accessory", "Gaps 🔍")

    // Automatically trigger initial analysis on view load if selected and not yet analyzed
    LaunchedEffect(selectedFilter) {
        if (selectedFilter == "Gaps 🔍" && analysis == null) {
            onTriggerAnalysis()
        }
    }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "C L O S E T",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = StyraDark,
                letterSpacing = 2.sp
            )
            
            if (selectedFilter == "Gaps 🔍" && analysis != null && !isAnalyzing) {
                IconButton(
                    onClick = { onTriggerAnalysis() },
                    modifier = Modifier.size(36.dp).testTag("refresh_analysis_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Re-analyze gaps",
                        tint = OliveDeep
                    )
                }
            }
        }

        // Horizontal scrollable filter list to ensure zero overflow on compact devices
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categories.forEach { cat ->
                val isSelected = selectedFilter == cat
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) OliveDeep else CreamLightContainer)
                        .clickable { selectedFilter = cat }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("filter_chip_${cat.lowercase().replace(" ", "_")}")
                ) {
                    Text(
                        text = cat,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else StyraDark
                    )
                }
            }
        }

        // Closet grid or Gap analysis content area
        if (selectedFilter == "Gaps 🔍") {
            if (isAnalyzing) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            color = OliveDeep,
                            strokeWidth = 3.dp,
                            modifier = Modifier.testTag("analysis_loader")
                        )
                        Text(
                            text = "Consulting Styra AI stylist...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = StyraDark
                        )
                        Text(
                            text = "Analyzing inventory coordinates & profile style preferences...",
                            fontSize = 11.sp,
                            color = GraySecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            } else if (analysis != null) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("wardrobe_analysis_panel"),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    // Wardrobe Completeness Score card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().testTag("completeness_card"),
                            colors = CardDefaults.cardColors(containerColor = OlivePale),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, OliveBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Wardrobe Health Score",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OliveDeep
                                    )
                                    Text(
                                        text = "${analysis.completenessScore}% COMPLETE",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        color = OliveDeep
                                    )
                                }

                                LinearProgressIndicator(
                                    progress = { analysis.completenessScore / 100f },
                                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).testTag("completeness_progress_bar"),
                                    color = OliveDeep,
                                    trackColor = Color.White
                                )

                                Text(
                                    text = analysis.feedback,
                                    fontSize = 12.sp,
                                    color = StyraDark,
                                    textAlign = TextAlign.Start,
                                    lineHeight = 18.sp,
                                    modifier = Modifier.padding(top = 4.dp).testTag("stylist_commentary")
                                )
                            }
                        }
                    }

                    // Section Title
                    item {
                        Text(
                            text = "Strategic Upgrades Needed (3 Gaps)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = StyraDark,
                            modifier = Modifier.padding(top = 4.dp).testTag("missing_items_section_header")
                        )
                    }

                    // Suggested clothes recommendations
                    items(analysis.recommendations) { rec ->
                        Card(
                            modifier = Modifier.fillMaxWidth().testTag("missing_item_card_${rec.name.lowercase().replace(" ", "_")}"),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = rec.name,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = StyraDark
                                        )
                                        Text(
                                            text = "Target Category: ${rec.type}",
                                            fontSize = 11.sp,
                                            color = GraySecondary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(CreamLightContainer)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = rec.priceRange,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = OliveDeep
                                        )
                                    }
                                }

                                Text(
                                    text = rec.whyNeeded,
                                    fontSize = 12.sp,
                                    color = Color.DarkGray,
                                    lineHeight = 17.sp,
                                    modifier = Modifier.testTag("why_needed_${rec.name.lowercase().replace(" ", "_")}")
                                )

                                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 0.5.dp)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "MATCHING COLORS",
                                            fontSize = 9.sp,
                                            color = GraySecondary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = rec.altColors.joinToString(", "),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = StyraDark
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "RECOMMENDED BRANDS",
                                            fontSize = 9.sp,
                                            color = GraySecondary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = rec.suggestedBrands.joinToString(", "),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = OliveDeep
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            val searchUrl = "https://www.google.com/search?q=${Uri.encode("buy " + rec.searchKeyword)}&tbm=shop"
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(searchUrl))
                                            context.startActivity(intent)
                                        },
                                        modifier = Modifier
                                            .weight(1.0f)
                                            .testTag("shop_item_button_${rec.name.lowercase().replace(" ", "_")}"),
                                        colors = ButtonDefaults.buttonColors(containerColor = StyraDark),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(vertical = 10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ShoppingCart,
                                            contentDescription = "Shop",
                                            modifier = Modifier.size(14.dp),
                                            tint = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Search Shop",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            onAddItem(
                                                rec.name,
                                                rec.type,
                                                rec.altColors.firstOrNull() ?: "Neutral",
                                                "All-season",
                                                "Casual",
                                                "Solid",
                                                rec.suggestedBrands.firstOrNull() ?: "Curated",
                                                "New",
                                                ""
                                            )
                                            Toast.makeText(context, "Added '${rec.name}' directly to your virtual Styra closet!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier
                                            .weight(1.0f)
                                            .testTag("add_gap_item_btn_${rec.name.lowercase().replace(" ", "_")}"),
                                        border = BorderStroke(1.dp, StyraBorderOutline),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StyraDark),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(vertical = 10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Add directly",
                                            modifier = Modifier.size(14.dp),
                                            tint = StyraDark
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Add to Closet",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = StyraDark
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Explore Wardrobe Gaps",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = StyraDark
                        )
                        Text(
                            text = "Styra will analyze your current clothing counts against your style preferences to reveal missing essential items.",
                            fontSize = 12.sp,
                            color = GraySecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        Button(
                            onClick = { onTriggerAnalysis() },
                            colors = ButtonDefaults.buttonColors(containerColor = OliveDeep),
                            modifier = Modifier.testTag("start_analysis_btn")
                        ) {
                            Text("Run Gap Analysis")
                        }
                    }
                }
            }
        } else {
            val filteredClothes = if (selectedFilter == "All") clothes else clothes.filter { it.type == selectedFilter }

            if (filteredClothes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No clothing items found matching filter. Tap camera button below to snap or add stylish pieces!",
                        fontSize = 12.sp,
                        color = GraySecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredClothes) { item ->
                        ClosetItemCard(item = item, onStatusChange = onStatusChange, onDelete = onDelete)
                    }
                }
            }
        }
    }
}

@Composable
fun ClosetItemCard(
    item: ClothingItem,
    onStatusChange: (ClothingItem, String) -> Unit,
    onDelete: (ClothingItem) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(0.5.dp, OliveBorder.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Category & Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.type.uppercase(),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = OliveDeep,
                    modifier = Modifier
                        .background(OlivePale, RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
                IconButton(
                    onClick = { onDelete(item) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove",
                        tint = Color.Red.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Image Thumbnail or Icon Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(86.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CreamLightContainer),
                contentAlignment = Alignment.Center
            ) {
                if (item.imageUri.isNotEmpty() && !item.imageUri.startsWith("default_")) {
                    AsyncImage(
                        model = item.imageUri,
                        contentDescription = item.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = when (item.type) {
                            "Top" -> "👕"
                            "Bottom" -> "👖"
                            "Shoes" -> "👞"
                            "Jacket" -> "🧥"
                            else -> "🕶️"
                        },
                        fontSize = 32.sp
                    )
                }
            }

            // Title & Brand info
            Column {
                Text(
                    text = item.name,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = StyraDark,
                    maxLines = 1
                )
                Text(
                    text = "${item.brand} • ${item.color}",
                    fontSize = 9.sp,
                    color = GraySecondary,
                    maxLines = 1
                )
            }

            // Laundry Toggle action
            val isClean = item.laundryStatus == "Clean"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isClean) OlivePale.copy(alpha = 0.5f) else Color.Red.copy(alpha = 0.08f))
                    .clickable {
                        onStatusChange(item, if (isClean) "Dirty" else "Clean")
                    }
                    .padding(vertical = 4.dp, horizontal = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isClean) Icons.Default.Check else Icons.Default.Layers,
                    contentDescription = "Status",
                    tint = if (isClean) OliveDeep else Color.Red,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isClean) "CLEAN" else "DIRTY (Laundering)",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isClean) OliveDeep else Color.Red
                )
            }
        }
    }
}

// --- Style Assessment Quiz Screen ---

@Composable
fun QuizScreen(
    profile: StyleProfile,
    onSave: (StyleProfile) -> Unit
) {
    var preferredStyle by remember { mutableStateOf(profile.preferredStyle) }
    var bodyFit by remember { mutableStateOf(profile.bodyFit) }
    var likedColors by remember { mutableStateOf(profile.likedColors) }
    var avoidedColors by remember { mutableStateOf(profile.avoidedColors) }
    var workDressCode by remember { mutableStateOf(profile.workDressCode) }
    var culturalStyle by remember { mutableStateOf(profile.culturalStyle) }
    var budget by remember { mutableStateOf(profile.budget) }
    var modesty by remember { mutableStateOf(profile.modesty) }
    var fashionGoals by remember { mutableStateOf(profile.fashionGoals) }

    val styles = listOf("Casual", "Minimalist", "Streetwear", "Classic", "Bohemian", "Retro")
    val fits = listOf("Slim", "Regular", "Loose")
    val dressCodes = listOf("Casual", "Smart Casual", "Business Formal")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        item {
            Text(
                text = "S T Y L E   Q U I Z",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = StyraDark,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Text(
                text = "Styra customizes outfits by learning your fit, preferences, work expectations, and style goals.",
                fontSize = 11.sp,
                color = GraySecondary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Q1 Preferred Style
        item {
            QuizQuestionTitle("1. What's your preferred aesthetics?")
            FlowRowSpacing(6.dp) {
                styles.forEach { style ->
                    val isSelected = preferredStyle == style
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) OliveDeep else CreamLightContainer)
                            .clickable { preferredStyle = style }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = style,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else StyraDark
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Q2 Body fit
        item {
            QuizQuestionTitle("2. Preferred body cut / fit?")
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                fits.forEach { fit ->
                    val isSelected = bodyFit == fit
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) OliveDeep else CreamLightContainer)
                            .clickable { bodyFit = fit }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = fit,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else StyraDark
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Q3 Colors like & avoid
        item {
            QuizQuestionTitle("3. Colors you love / avoid?")
            OutlinedTextField(
                value = likedColors,
                onValueChange = { likedColors = it },
                label = { Text("Favorite Colors (comma separated)", fontSize = 11.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("loved_colors_field"),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = avoidedColors,
                onValueChange = { avoidedColors = it },
                label = { Text("Avoid / Disliked Colors", fontSize = 11.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Q4 Dress code
        item {
            QuizQuestionTitle("4. Standard workplace dress code?")
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                dressCodes.forEach { dc ->
                    val isSelected = workDressCode == dc
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) OliveDeep else CreamLightContainer)
                            .clickable { workDressCode = dc }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = dc,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else StyraDark,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Modesty & Goals
        item {
            QuizQuestionTitle("5. Modesty and style objectives")
            OutlinedTextField(
                value = modesty,
                onValueChange = { modesty = it },
                label = { Text("Modesty profile (e.g. Standard, Full sleeves, Low cut, etc.)", fontSize = 11.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = fashionGoals,
                onValueChange = { fashionGoals = it },
                label = { Text("Primary fashion goals (e.g., Confidence, Minimalist packing)", fontSize = 11.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Save
        item {
            Button(
                onClick = {
                    val updated = StyleProfile(
                        preferredStyle = preferredStyle,
                        bodyFit = bodyFit,
                        likedColors = likedColors,
                        avoidedColors = avoidedColors,
                        workDressCode = workDressCode,
                        culturalStyle = culturalStyle,
                        budget = budget,
                        modesty = modesty,
                        fashionGoals = fashionGoals
                    )
                    onSave(updated)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_quiz_button"),
                colors = ButtonDefaults.buttonColors(containerColor = OliveDeep),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "SAVE STYLE PROFILE",
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun QuizQuestionTitle(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = StyraDark,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

// FlowRow spacing wrapper
@Composable
fun FlowRowSpacing(spacing: androidx.compose.ui.unit.Dp, content: @Composable () -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            content()
        }
    }
}

// --- Dynamic Calendar Screen ---

@Composable
fun CalendarScreen(
    events: List<CalendarEvent>,
    onAddEvent: (String, String, String, String, String) -> Unit,
    onDeleteEvent: (CalendarEvent) -> Unit,
    onSelectEvent: (CalendarEvent) -> Unit,
    currentSimulatedTitle: String
) {
    var title by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Work Meeting") }
    var date by remember { mutableStateOf("May 21, 2026") }
    var time by remember { mutableStateOf("11:00 AM") }

    val categories = listOf("Work Meeting", "Date Night", "Church", "Gym", "Wedding", "Travel", "Interview", "Casual Weekend")

    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        item {
            Text(
                text = "A G E N D A",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = StyraDark,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Text(
                text = "Styra analyzes upcoming calendar items automatically to prepare appropriate garments.",
                fontSize = 11.sp,
                color = GraySecondary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // Sync local device calendar CTA
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(OlivePale.copy(alpha = 0.4f))
                    .border(BorderStroke(1.dp, OliveBorder), RoundedCornerShape(20.dp))
                    .clickable {
                        // Directly trigger sync simulated message or fetch real database cursor
                        onAddEvent(
                            "Coffee Network",
                            "Networking Event",
                            "Coffee meet up with the VP of Architecture.",
                            "May 23, 2026",
                            "09:00 AM"
                        )
                        Toast
                            .makeText(
                                context,
                                "Google Calendar synced! Synchronized Coffee Network to your Styra agenda.",
                                Toast.LENGTH_LONG
                            )
                            .show()
                    }
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = "Sync Google Calendar",
                        tint = OliveDeep
                    )
                    Text(
                        text = "SYNC GOOGLE/OUTLOOK CALENDAR",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = OliveDeep,
                        letterSpacing = 0.5.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Add Local Event Form
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        "Plan Custom Dressing Event",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = StyraDark
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Agenda Title (e.g. Dream Interview)", fontSize = 11.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("calendar_event_title_field"),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = details,
                        onValueChange = { details = it },
                        label = { Text("Specific expectations / dress rules", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Choose Category
                    Text("Occasion Formality:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GraySecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val rowCategories = categories.take(4)
                        rowCategories.forEach { cat ->
                            val isSelected = selectedCategory == cat
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) OliveDeep else CreamLightContainer)
                                    .clickable { selectedCategory = cat }
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cat.replace(" Meeting", ""),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else StyraDark
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (title.isNotEmpty()) {
                                onAddEvent(title, selectedCategory, details, date, time)
                                title = ""
                                details = ""
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("add_calendar_event"),
                        colors = ButtonDefaults.buttonColors(containerColor = OliveDeep),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("SCHEDULE STYLING ADVICE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Active Agenda Events headers
        item {
            Text(
                text = "Upcoming Scheduled Outfits",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = StyraDark,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (events.isEmpty()) {
            item {
                Text(
                    text = "No custom events scheduled yet.",
                    fontSize = 12.sp,
                    color = GraySecondary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            items(events) { event ->
                val isActiveSim = currentSimulatedTitle == event.title
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isActiveSim) OlivePale.copy(alpha = 0.5f) else Color.White)
                        .border(
                            BorderStroke(
                                if (isActiveSim) 1.5.dp else 1.dp,
                                if (isActiveSim) OliveDeep else Color.LightGray.copy(alpha = 0.4f)
                            ),
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { onSelectEvent(event) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (isActiveSim) OliveDeep else CreamLightContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when {
                                    event.eventType.lowercase().contains("work") -> "💼"
                                    event.eventType.lowercase().contains("date") -> "🥂"
                                    event.eventType.lowercase().contains("gym") -> "🏋️"
                                    event.eventType.lowercase().contains("wedding") -> "👰"
                                    else -> "📍"
                                },
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = event.title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = StyraDark
                            )
                            Text(
                                text = "${event.date} • ${event.time} (${event.eventType})",
                                fontSize = 10.sp,
                                color = GraySecondary
                            )
                            if (event.details.isNotEmpty()) {
                                Text(
                                    text = "Note: ${event.details}",
                                    fontSize = 9.sp,
                                    color = StyraDark.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isActiveSim) {
                            Text(
                                "ACTIVE FIT",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = OliveDeep,
                                modifier = Modifier
                                    .background(Color.White, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        IconButton(onClick = { onDeleteEvent(event) }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Delete event",
                                tint = Color.Red.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- Profile / Rating Screen ---

@Composable
fun ProfileScreen(
    savedOutfits: List<SavedOutfit>,
    clothes: List<ClothingItem>,
    userEmail: String,
    onDeleteSaved: (SavedOutfit) -> Unit
) {
    val cleanPercent = if (clothes.isNotEmpty()) {
        (clothes.filter { it.laundryStatus == "Clean" }.size * 100) / clothes.size
    } else 100

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        item {
            // User section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(OliveDeep),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CO",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Carl Anthony Osborne",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = StyraDark
                )
                Text(
                    text = userEmail,
                    fontSize = 11.sp,
                    color = GraySecondary
                )
            }
        }

        // Stats Box
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(CreamLightContainer)
                    .border(1.dp, OliveBorder, RoundedCornerShape(20.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Clothes", fontSize = 10.sp, color = GraySecondary)
                    Text("${clothes.size} Pcs", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = StyraDark)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Laundry Ready", fontSize = 10.sp, color = GraySecondary)
                    Text("$cleanPercent% Clean", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = OliveDeep)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Saved Outfits", fontSize = 10.sp, color = GraySecondary)
                    Text("${savedOutfits.size} saved", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = StyraDark)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Saved list
        item {
            Text(
                text = "My Stylist Bookmarks (Saved Outfits)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = StyraDark,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        if (savedOutfits.isEmpty()) {
            item {
                Text(
                    text = "No saved layouts yet. Click star inside style matching cards to bookmark gorgeous looks!",
                    fontSize = 11.sp,
                    color = GraySecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            items(savedOutfits) { outfit ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = outfit.title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = StyraDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Occasion: ${outfit.eventName} • ${outfit.weatherDescription}",
                            fontSize = 9.sp,
                            color = GraySecondary
                        )
                        Text(
                            text = outfit.commentary,
                            fontSize = 10.sp,
                            lineHeight = 14.sp,
                            color = StyraDark.copy(alpha = 0.82f),
                            maxLines = 2
                        )
                    }
                    IconButton(onClick = { onDeleteSaved(outfit) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove Bookmark",
                            tint = Color.Red.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

// --- Dialogs ---

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddItemDialog(
    viewModel: FashionViewModel,
    onDismiss: () -> Unit,
    onItemAdded: (String, String, String, String, String, String, String, String, String) -> Unit
) {
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Top") }
    var color by remember { mutableStateOf("") }
    var season by remember { mutableStateOf("All") }
    var formality by remember { mutableStateOf("Casual") }
    var pattern by remember { mutableStateOf("Solid") }
    var brand by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf("Good") }

    val context = LocalContext.current
    val aiTaggingResult by viewModel.aiTaggingResult.collectAsStateWithLifecycle()
    val isAiTagging by viewModel.isAiTagging.collectAsStateWithLifecycle()

    // Handle incoming AI tagging updates
    LaunchedEffect(aiTaggingResult) {
        val result = aiTaggingResult
        if (result != null && result.success) {
            name = result.name
            selectedType = result.type
            color = result.color
            season = result.season
            formality = result.formality
            pattern = result.pattern
            brand = result.brand
            condition = result.condition
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            photoUri = uri
            viewModel.analyzeNewClothingPhoto(context, uri)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotEmpty()) {
                        onItemAdded(
                            name,
                            selectedType,
                            color.ifEmpty { "Neutral" },
                            season,
                            formality,
                            pattern,
                            brand.ifEmpty { "Zara" },
                            condition,
                            photoUri?.toString() ?: ""
                        )
                    }
                },
                modifier = Modifier.testTag("submit_clothing_item"),
                colors = ButtonDefaults.buttonColors(containerColor = OliveDeep)
            ) {
                Text("ADD TO CLOSET", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = GraySecondary)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = BoneWhite,
        title = {
            Text(
                "Snap / Add Wardrobe Piece",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = StyraDark
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Photo Snapping Banner
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(OlivePale.copy(alpha = 0.4f))
                            .border(BorderStroke(1.2.dp, OliveBorder), RoundedCornerShape(14.dp))
                            .clickable { galleryLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (photoUri != null) {
                            AsyncImage(
                                model = photoUri,
                                contentDescription = "Active upload",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = "Snap camera icon",
                                    tint = OliveDeep,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "SNAP PHOTO FOR AUTO-TAGGING",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OliveDeep
                                )
                                Text(
                                    "Gemini will auto-classify everything",
                                    fontSize = 8.sp,
                                    color = GraySecondary
                                )
                            }
                        }
                    }
                }

                // Loader during AI classification
                if (isAiTagging) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CreamLightContainer, RoundedCornerShape(10.dp))
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = OliveDeep, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Gemini AI is scanning image attributes...",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = OliveDeep
                            )
                        }
                    }
                }

                if (aiTaggingResult != null) {
                    item {
                        val isSim = aiTaggingResult?.isSimulated ?: false
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(OlivePale.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, "AI icon", tint = OliveDeep, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isSim) "Simulated predictive tagging loaded!" else "AI classification complete!",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = OliveDeep
                            )
                        }
                    }
                }

                // Core Name
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Garment Name", fontSize = 11.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("clothing_name_field"),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // Type Categorization
                item {
                    Text("Garment Type Category", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Top", "Bottom", "Shoes", "Jacket", "Accessory").forEach { type ->
                            val isSel = selectedType == type
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) OliveDeep else CreamLightContainer)
                                    .clickable { selectedType = type }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    type,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.White else StyraDark
                                )
                            }
                        }
                    }
                }

                // Color Descriptor
                item {
                    OutlinedTextField(
                        value = color,
                        onValueChange = { color = it },
                        label = { Text("Primary Color (e.g. Navy, Beige)", fontSize = 11.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("color_field"),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = brand,
                        onValueChange = { brand = it },
                        label = { Text("Brand guess (e.g., Uniqlo)", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // Details Row: Formality & Season
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = formality,
                                onValueChange = { formality = it },
                                label = { Text("Formality", fontSize = 10.sp) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = season,
                                onValueChange = { season = it },
                                label = { Text("Season", fontSize = 10.sp) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun WeatherSimulatorDialog(
    currentTemp: String,
    currentCond: String,
    currentHum: String,
    currentWind: String,
    currentUv: String,
    onDismiss: () -> Unit,
    onApply: (String, String, String, String, String) -> Unit
) {
    var temp by remember { mutableStateOf(currentTemp.replace("°F", "")) }
    var cond by remember { mutableStateOf(currentCond) }
    var hum by remember { mutableStateOf(currentHum) }
    var wind by remember { mutableStateOf(currentWind) }
    var uv by remember { mutableStateOf(currentUv) }

    val conditions = listOf("Partly Cloudy", "Rainy Stormy", "Sizzling Hot", "Breezy Autumn", "Chilly Winter", "Sunny & High UV")

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    onApply("$temp°F", cond, hum, wind, uv)
                },
                modifier = Modifier.testTag("apply_weather_sim"),
                colors = ButtonDefaults.buttonColors(containerColor = OliveDeep)
            ) {
                Text("SHIFT CLIMATE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = GraySecondary)
            }
        },
        containerColor = BoneWhite,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text("Climate & Weather Simulator", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Styra alters styling tips instantly to match custom temperatures, humidity factors, and wet or freezing climates.",
                    fontSize = 10.sp,
                    color = GraySecondary
                )

                OutlinedTextField(
                    value = temp,
                    onValueChange = { temp = it },
                    label = { Text("Temperature (°F)", fontSize = 11.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Text("Predefined preset conditions:", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        conditions.chunked(3).forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                row.forEach { item ->
                                    val isSel = cond == item
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSel) OliveDeep else CreamLightContainer)
                                            .clickable {
                                                cond = item
                                                when (item) {
                                                    "Sizzling Hot" -> {
                                                        temp = "92"
                                                        hum = "22%"
                                                        wind = "4 mph"
                                                        uv = "Extreme"
                                                    }
                                                    "Rainy Stormy" -> {
                                                        temp = "58"
                                                        hum = "95%"
                                                        wind = "14 mph"
                                                        uv = "Low"
                                                    }
                                                    "Breezy Autumn" -> {
                                                        temp = "66"
                                                        hum = "40%"
                                                        wind = "12 mph"
                                                        uv = "Moderate"
                                                    }
                                                    "Chilly Winter" -> {
                                                        temp = "34"
                                                        hum = "85%"
                                                        wind = "18 mph"
                                                        uv = "Low"
                                                    }
                                                    else -> {
                                                        temp = "68"
                                                        hum = "42%"
                                                        wind = "8 mph"
                                                        uv = "Moderate"
                                                    }
                                                }
                                            }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            item,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSel) Color.White else StyraDark,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

// --- Android Sharesheet Sharing Helper ---

fun shareOutfitToSocials(
    context: Context,
    title: String,
    top: String,
    bottom: String,
    shoes: String,
    commentary: String
) {
    val shareText = """
        ✨ Stylist recommendation from Styra ✨
        👉 Title of the Day: "$title"
        👕 Top Wear: $top
        👖 Bottom Wear: $bottom
        👞 Footwear: $shoes
        
        💎 Commentary: "$commentary"
        
        Generated by Styra - Curating fashion choices effortlessly. 🚀
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "My Styled Outfit from Styra")
        putExtra(Intent.EXTRA_TEXT, shareText)
    }

    val chooser = Intent.createChooser(intent, "Share Styled Layout via")
    context.startActivity(chooser)
}
