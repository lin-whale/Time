package com.example.time

//import android.graphics.Color
//import android.widget.Button
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.example.time.logic.model.AppDatabase
import com.example.time.logic.model.LifePiece
import com.example.time.logic.model.TimePiece
import com.example.time.ui.TimeViewModel
import com.example.time.ui.TimeViewModelFactory
import com.example.time.ui.timeRecord.InputField
import com.example.time.ui.timeRecord.LifeList
import com.example.time.ui.timeRecord.TimeAPPMainLayout
import com.example.time.ui.timeRecord.TimePickerDialog
import com.example.tiptime.ui.theme.TimeTheme



class MainActivity : ComponentActivity() {

    private val lifePieceViewModel: TimeViewModel by viewModels {
        TimeViewModelFactory((application as LifePieceApplication).repository)
    }

    //    private val wordViewModel: WordViewModel by viewModels {
//        WordViewModelFactory((application as WordsApplication).repository)
//    }
//    private val lifePieceViewModel: LifePieceViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContent{LifePieceScreen(viewModel = lifePieceViewModel)}

        enableEdgeToEdge()
        setContent {
            TimeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    TimeAPPMainLayout(viewModel = lifePieceViewModel)
                }
            }
        }

    }

}



//@Preview(showBackground = true)
//@Composable
//fun TipTimeLayoutPreview(@PreviewParameter(YourDatabaseProvider::class) db: AppDatabase = TimeApplication().db) {
//    TimeTheme {
//        TimeAPPLayout(db = db)
//    }
//}