@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.skillswap.app
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import android.app.Activity
import android.util.Log
import android.widget.Toast

import androidx.compose.runtime.rememberCoroutineScope
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException

import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

import kotlinx.coroutines.launch

private val SkillSwapBlue = Color(0xFF2563EB)
private val SkillSwapBlueDark = Color(0xFF1D4ED8)
private val SkillSwapBlueLight = Color(0xFFEFF6FF)
private val SkillSwapBlack = Color(0xFF111827)
private val SkillSwapWhite = Color(0xFFFFFFFF)

private val SkillSwapColors = lightColorScheme(
    primary = SkillSwapBlue,
    onPrimary = SkillSwapWhite,
    primaryContainer = SkillSwapBlueLight,
    onPrimaryContainer = SkillSwapBlueDark,

    background = SkillSwapWhite,
    onBackground = SkillSwapBlack,

    surface = SkillSwapWhite,
    onSurface = SkillSwapBlack,

    surfaceVariant = Color(0xFFF3F4F6),
    onSurfaceVariant = Color(0xFF6B7280)
)


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme(
                colorScheme = SkillSwapColors
            ) {
                SkillSwapApp(this@MainActivity)
            }
        }
    }
}


/* ================================================= */
/* APP SCREEN */
/* ================================================= */

enum class AppScreen {
    PROFILE,
    MATCH,
    CONNECT,
    SESSION,
    RESULT
}


/* ================================================= */
/* MAIN APP */
/* ================================================= */

@Composable
fun SkillSwapApp(activity: Activity) {

    var showIntro by remember {
        mutableStateOf(true)
    }
    var isSignedIn by remember {
        mutableStateOf(FirebaseAuth.getInstance().currentUser != null)
    }

    var currentScreen by remember {
        mutableStateOf(AppScreen.PROFILE)
    }

    var name by remember {
        mutableStateOf("")
    }

    var teachSkill by remember {
        mutableStateOf("")
    }

    var learnSkill by remember {
        mutableStateOf("")
    }

    LaunchedEffect(Unit) {
        delay(1800)
        showIntro = false
    }

    androidx.compose.animation.AnimatedContent(
        targetState = showIntro,
        transitionSpec = {
            fadeIn(
                animationSpec = tween(500)
            ) togetherWith fadeOut(
                animationSpec = tween(500)
            )
        },
        label = "startup_transition"
    ) { introVisible ->

        if (introVisible) {

            SkillSwapIntro()

        } else if (!isSignedIn) {

            GoogleSignInScreen(
                activity = activity,
                onSignedIn = {
                    isSignedIn = true
                }
            )

        } else {

            when (currentScreen) {

                AppScreen.PROFILE -> {

                    ProfileScreen(
                        name = name,
                        teachSkill = teachSkill,
                        learnSkill = learnSkill,

                        onNameChange = {
                            name = it
                        },

                        onTeachChange = {
                            teachSkill = it
                        },

                        onLearnChange = {
                            learnSkill = it
                        },

                        onFindMatch = {
                            currentScreen = AppScreen.MATCH
                        }
                    )
                }

                AppScreen.MATCH -> {

                    MatchScreen(
                        teachSkill = teachSkill,
                        learnSkill = learnSkill,
                        onConnect = {
                            currentScreen = AppScreen.CONNECT
                        }
                    )
                }

                AppScreen.CONNECT -> {

                    ConnectScreen(
                        teachSkill = teachSkill,
                        learnSkill = learnSkill,
                        onGenerateSession = {
                            currentScreen = AppScreen.SESSION
                        }
                    )
                }

                AppScreen.SESSION -> {

                    SessionScreen(
                        onComplete = {
                            currentScreen = AppScreen.RESULT
                        }
                    )
                }

                AppScreen.RESULT -> {

                    ResultScreen(
                        onStartAgain = {
                            currentScreen = AppScreen.PROFILE
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SkillSwapIntro() {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SkillSwapBlue),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            AnimatedVisibility(
                visible = true,
                enter = fadeIn(
                    animationSpec = tween(700)
                ) + scaleIn(
                    initialScale = 0.75f,
                    animationSpec = tween(700)
                )
            ) {

                Surface(
                    modifier = Modifier
                        .width(92.dp)
                        .height(92.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = SkillSwapWhite
                ) {

                    Box(
                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = "SS",
                            color = SkillSwapBlue,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            AnimatedVisibility(
                visible = true,
                enter = fadeIn(
                    animationSpec = tween(
                        durationMillis = 800,
                        delayMillis = 250
                    )
                )
            ) {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "SkillSwap",
                        color = SkillSwapWhite,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = "Learn. Teach. Exchange.",
                        color = SkillSwapWhite.copy(alpha = 0.85f),
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}


/* ================================================= */
/* PROFILE SCREEN */
/* ================================================= */

@Composable
fun ProfileScreen(
    name: String,
    teachSkill: String,
    learnSkill: String,
    onNameChange: (String) -> Unit,
    onTeachChange: (String) -> Unit,
    onLearnChange: (String) -> Unit,
    onFindMatch: () -> Unit
) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "SkillSwap",
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = SkillSwapBlueLight
                ) {

                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {

                        Text(
                            text = "YOUR SKILLS",
                            color = SkillSwapBlue,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Text(
                            text = "Learn. Teach. Exchange.",
                            fontSize = 30.sp,
                            lineHeight = 36.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SkillSwapBlack
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Text(
                            text = "Tell us what you know and what you want to learn. AI will help you discover the right person.",
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {

                Text(
                    text = "About you",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            item {

                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Your name")
                    },
                    placeholder = {
                        Text("e.g. Shivesh")
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
            }

            item {

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {

                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {

                        Text(
                            text = "🎓 What can you teach?",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.height(10.dp)
                        )

                        OutlinedTextField(
                            value = teachSkill,
                            onValueChange = onTeachChange,
                            modifier = Modifier.fillMaxWidth(),
                            label = {
                                Text("Skill you can teach")
                            },
                            placeholder = {
                                Text("e.g. Python")
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
            }

            item {

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {

                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {

                        Text(
                            text = " What do you want to learn?",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.height(10.dp)
                        )

                        OutlinedTextField(
                            value = learnSkill,
                            onValueChange = onLearnChange,
                            modifier = Modifier.fillMaxWidth(),
                            label = {
                                Text("Skill you want to learn")
                            },
                            placeholder = {
                                Text("e.g. French")
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
            }

            item {

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Button(
                    onClick = onFindMatch,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {

                    Text(
                        text = "✨ Find My AI Match",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item {

                Text(
                    text = "Everyone knows something. Everyone has something to learn.",
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 4.dp,
                            bottom = 24.dp
                        )
                )
            }
        }
    }
}

/* ================================================= */
/* AI MATCH SCREEN */
/* ================================================= */

@Composable
fun MatchScreen(
    teachSkill: String,
    learnSkill: String,
    onConnect: () -> Unit
) {

    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text("AI Matchmaker")
                }
            )
        }

    ) { padding ->

        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
        ) {

            Text(
                text = "We found your match!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "Gemini analyses skills, goals, levels and availability."
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )


            /* MATCH CARD */

            Card(

                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(24.dp)
            ) {

                Column(

                    modifier = Modifier.padding(24.dp)
                ) {

                    Text(
                        text = "Emma",
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Linguistics Student",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(
                        modifier = Modifier.height(20.dp)
                    )

                    Text(
                        text = "94% MATCH",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    Text(
                        text = "Emma teaches: $learnSkill"
                    )

                    Text(
                        text = "Emma wants to learn: $teachSkill"
                    )

                    Spacer(
                        modifier = Modifier.height(18.dp)
                    )

                    Text(
                        text = "Why this match?",
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text("✓ Reciprocal skill exchange")

                    Text("✓ Compatible learning goals")

                    Text("✓ Suitable skill levels")

                    Text("✓ Compatible availability")
                }
            }


            Spacer(
                modifier = Modifier.weight(1f)
            )


            /* CONNECT BUTTON */

            Button(

                onClick = onConnect,

                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),

                shape = RoundedCornerShape(16.dp)
            ) {

                Text(
                    text = "Connect with Emma",
                    fontSize = 17.sp
                )
            }
        }
    }
}


/* ================================================= */
/* CONNECT SCREEN */
/* ================================================= */

@Composable
fun ConnectScreen(
    teachSkill: String,
    learnSkill: String,
    onGenerateSession: () -> Unit
) {

    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text("Connect")
                }
            )
        }

    ) { padding ->

        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),

            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(
                modifier = Modifier.height(40.dp)
            )


            Text(
                text = "You're a match! 🎉",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )


            Spacer(
                modifier = Modifier.height(12.dp)
            )


            Text(
                text = "You teach $teachSkill.\nEmma teaches $learnSkill.",
                textAlign = TextAlign.Center
            )

            Spacer(
                modifier = Modifier.height(30.dp)
            )


            /* EXCHANGE CARD */

            Card(

                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(22.dp)
            ) {

                Column(

                    modifier = Modifier.padding(25.dp)
                ) {

                    Text(
                        text = "Skill Exchange",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold
                    )


                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )


                    Text(
                        text = "YOU → EMMA",
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = teachSkill
                    )


                    Spacer(
                        modifier = Modifier.height(15.dp)
                    )


                    Text(
                        text = "EMMA → YOU",
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = learnSkill
                    )
                }
            }


            Spacer(
                modifier = Modifier.weight(1f)
            )


            /* SESSION BUTTON */

            Button(

                onClick = onGenerateSession,

                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),

                shape = RoundedCornerShape(16.dp)
            ) {

                Text(
                    text = "Generate AI Session",
                    fontSize = 17.sp
                )
            }
        }
    }
}


/* ================================================= */
/* SESSION SCREEN */
/* ================================================= */

@Composable
fun SessionScreen(
    onComplete: () -> Unit
) {

    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text("AI Session Coach")
                }
            )
        }

    ) { padding ->

        LazyColumn(

            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),

            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            item {

                Text(
                    text = "45-Minute Session",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "AI-generated co-learning agenda."
                )
            }


            item {

                SessionItem(
                    time = "5 min",
                    title = "Warm-up",
                    description = "Icebreaker and introductions"
                )
            }


            item {

                SessionItem(
                    time = "10 min",
                    title = "Python Lesson",
                    description = "Functions and loops"
                )
            }


            item {

                SessionItem(
                    time = "15 min",
                    title = "Joint Practice",
                    description = "Solve a small coding exercise"
                )
            }


            item {

                SessionItem(
                    time = "10 min",
                    title = "French Practice",
                    description = "Conversation practice"
                )
            }


            item {

                SessionItem(
                    time = "5 min",
                    title = "AI Quiz",
                    description = "Quick knowledge check"
                )
            }


            item {

                Button(

                    onClick = onComplete,

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),

                    shape = RoundedCornerShape(16.dp)
                ) {

                    Text(
                        text = "Complete Session",
                        fontSize = 17.sp
                    )
                }
            }
        }
    }
}


/* ================================================= */
/* SESSION ITEM */
/* ================================================= */

@Composable
fun SessionItem(
    time: String,
    title: String,
    description: String
) {

    Card(

        modifier = Modifier.fillMaxWidth(),

        shape = RoundedCornerShape(18.dp)
    ) {

        Row(

            modifier = Modifier.padding(18.dp),

            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(

                shape = RoundedCornerShape(12.dp),

                color = MaterialTheme.colorScheme.primaryContainer
            ) {

                Text(

                    text = time,

                    fontWeight = FontWeight.Bold,

                    modifier = Modifier.padding(12.dp)
                )
            }


            Spacer(
                modifier = Modifier.width(15.dp)
            )


            Column {

                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )

                Text(
                    text = description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


/* ================================================= */
/* RESULT SCREEN */
/* ================================================= */

@Composable
fun ResultScreen(
    onStartAgain: () -> Unit
) {

    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text("Session Complete")
                }
            )
        }

    ) { padding ->

        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(25.dp),

            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(
                modifier = Modifier.height(50.dp)
            )


            Text(
                text = "🎉",
                fontSize = 70.sp
            )


            Text(
                text = "Great work!",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )


            Spacer(
                modifier = Modifier.height(10.dp)
            )


            Text(
                text = "You completed your SkillSwap session."
            )


            Spacer(
                modifier = Modifier.height(30.dp)
            )


            /* XP CARD */

            Card(

                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(24.dp)
            ) {

                Column(

                    modifier = Modifier.padding(25.dp),

                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "+100 XP",
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )


                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )


                    Text(
                        text = "🏆 Skill Explorer Badge",
                        fontSize = 18.sp
                    )


                    Spacer(
                        modifier = Modifier.height(25.dp)
                    )


                    Text(
                        text = "How was your session?"
                    )


                    Spacer(
                        modifier = Modifier.height(15.dp)
                    )


                    Text(
                        text = "😞    😐    😊    🤩",
                        fontSize = 30.sp
                    )
                }
            }


            Spacer(
                modifier = Modifier.weight(1f)
            )


            /* START AGAIN */

            Button(

                onClick = onStartAgain,

                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),

                shape = RoundedCornerShape(16.dp)
            ) {

                Text(
                    text = "Find Another Skill",
                    fontSize = 17.sp
                )
            }
        }
    }
}
/* ================================================= */
/* GOOGLE SIGN-IN */
/* ================================================= */

@Composable
fun GoogleSignInScreen(
    activity: Activity,
    onSignedIn: () -> Unit
) {

    val context = activity
    val scope = rememberCoroutineScope()
    val credentialManager = CredentialManager.create(context)

    var isLoading by remember {
        mutableStateOf(false)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = SkillSwapWhite
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Surface(
                modifier = Modifier
                    .width(90.dp)
                    .height(90.dp),
                shape = RoundedCornerShape(28.dp),
                color = SkillSwapBlue
            ) {

                Box(
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = "SS",
                        color = SkillSwapWhite,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Text(
                text = "Welcome to SkillSwap",
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Text(
                text = "Learn skills. Teach skills. Meet your perfect learning partner.",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 16.sp
            )

            Spacer(
                modifier = Modifier.height(40.dp)
            )

            Button(
                onClick = {

                    if (isLoading) return@Button

                    isLoading = true

                    scope.launch {

                        try {

                            val signInWithGoogleOption =
                                GetSignInWithGoogleOption.Builder(
                                    serverClientId = context.getString(
                                        R.string.default_web_client_id
                                    )
                                ).build()

                            val request = GetCredentialRequest.Builder()
                                .addCredentialOption(signInWithGoogleOption)
                                .build()

                            val result = credentialManager.getCredential(
                                context = context,
                                request = request
                            )

                            val credential = result.credential

                            if (
                                credential is CustomCredential &&
                                credential.type ==
                                GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                            ) {

                                val googleIdTokenCredential =
                                    GoogleIdTokenCredential.createFrom(
                                        credential.data
                                    )

                                val firebaseCredential =
                                    GoogleAuthProvider.getCredential(
                                        googleIdTokenCredential.idToken,
                                        null
                                    )

                                FirebaseAuth.getInstance()
                                    .signInWithCredential(firebaseCredential)
                                    .addOnCompleteListener { task ->

                                        isLoading = false

                                        if (task.isSuccessful) {

                                            val user =
                                                FirebaseAuth.getInstance()
                                                    .currentUser

                                            Log.d(
                                                "SkillSwapAuth",
                                                "Google sign-in successful: ${user?.uid}"
                                            )

                                            Toast.makeText(
                                                context,
                                                "Welcome to SkillSwap!",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            onSignedIn()

                                        } else {

                                            Log.e(
                                                "SkillSwapAuth",
                                                "Firebase sign-in failed",
                                                task.exception
                                            )

                                            Toast.makeText(
                                                context,
                                                "Sign-in failed. Please try again.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }

                            } else {

                                isLoading = false

                                Toast.makeText(
                                    context,
                                    "Invalid Google credential.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                        } catch (e: GetCredentialException) {

                            isLoading = false

                            Log.e(
                                "SkillSwapAuth",
                                "Credential error",
                                e
                            )

                            Toast.makeText(
                                context,
                                "Google sign-in cancelled or failed.",
                                Toast.LENGTH_LONG
                            ).show()

                        } catch (e: GoogleIdTokenParsingException) {

                            isLoading = false

                            Log.e(
                                "SkillSwapAuth",
                                "Google token parsing failed",
                                e
                            )

                            Toast.makeText(
                                context,
                                "Could not read Google account.",
                                Toast.LENGTH_LONG
                            ).show()

                        } catch (e: Exception) {

                            isLoading = false

                            Log.e(
                                "SkillSwapAuth",
                                "Unexpected sign-in error",
                                e
                            )

                            Toast.makeText(
                                context,
                                "Something went wrong.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                },

                enabled = !isLoading,

                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),

                shape = RoundedCornerShape(16.dp)
            ) {

                Text(
                    text = if (isLoading)
                        "Signing in..."
                    else
                        "Continue with Google",

                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            Text(
                text = "Your Google password is never stored by SkillSwap.",
                textAlign = TextAlign.Center,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}