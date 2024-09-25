package com.example.notetaking

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.notetaking.ui.theme.NotetakingTheme
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    //database will be later initialized
    private lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()

        database = Firebase.database.reference
        setContent {
            NotetakingTheme {
                NoteTakingApp(database)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteTakingApp(database: DatabaseReference) {

    var note by remember { mutableStateOf("") }
    //sort by date
    var latestNote by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        fetchLatestNoteFromDatabase(database) { fetchedNote ->
            latestNote = fetchedNote

        }
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("Note Taking App") })
        },
        content = { paddingValues ->
            NoteTakingScreen(
                note = note,
                latestNote = latestNote,
                onNoteChange = { note = it },
                onSaveNote = {
                    SaveNoteToDatabase(note, database)
                    note = ""
                },
                // maintain proper UI
                paddingValues = paddingValues,
            )
        }
    )
}


@Composable
fun NoteTakingScreen(
    note: String,
    latestNote: String,
    onNoteChange: (String) -> Unit,
    onSaveNote: () -> Unit,
    paddingValues: PaddingValues,){

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {

        TextField(value = note,
            onValueChange = onNoteChange,
            label = { Text("Enter Note") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.padding(16.dp))

        Button(
            onClick = onSaveNote,
            modifier = Modifier.align(Alignment.End)
        ){
            Text("Save Note")

        }
        Spacer(modifier = Modifier.padding(32.dp))
        Text(
            text = "Latest Note: $latestNote",
            style = MaterialTheme.typography.titleLarge
        )

    }
}

fun SaveNoteToDatabase(note: String, database: DatabaseReference) {
    val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    val currentTime = sdf.format(Date())
    database.child("notes").child(currentTime).setValue(note)
        .addOnSuccessListener {
            Log.d("Firebase", "Note saved successfully")
        }
        .addOnFailureListener { exception ->
            Log.e("Firebase", "Error saving note", exception)
        }
}

fun fetchLatestNoteFromDatabase(database: DatabaseReference, onResult: (String) -> Unit) {

    //callback for the query finish
    //search note notes and limit by
    database.child("notes").get()
        .addOnSuccessListener { snapshot ->
            if(snapshot.exists()){
                val noteValues = snapshot.children.last().getValue(String::class.java)?: ""
                Log.d("Firebase", "Fetch Latest Note: $noteValues")
                onResult(noteValues)
            } else{
                Log.d("Firebase", "No notes found")
                onResult("")
            }
        }
        //log the error
        .addOnFailureListener { exception ->
            Log.e("Firebase", "Error fetching latest note", exception)
            onResult("")
        }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {

    val fakeDatabase = Firebase.database.reference
    NotetakingTheme {
        NoteTakingApp(fakeDatabase)
    }
}