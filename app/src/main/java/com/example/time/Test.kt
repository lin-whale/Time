import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextField
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ButtonList(labels: List<String>) {
    FlowRow(
        modifier = Modifier.fillMaxSize()
    ) {
        labels.forEach { label ->
            Button(
                onClick = { /* 按钮点击时执行的操作 */ },
                modifier = Modifier.padding(1.dp)
            ) {
                Text(text = label, fontSize = 6.sp, color = Color.White)
            }
        }
    }
}

@Preview
@Composable
fun PreviewButtonList() {
    val labels = remember { listOf("按钮1", "按钮2151615", "按钮3", "按钮1", "按钮2", "按钮3") }
    ButtonList(labels = labels)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTextToDatabase() {
    var text by remember { mutableStateOf("") }
    var showTextField by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showTextField) {
            TextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Enter text") }
            )

            Button(onClick = {
//                dbViewModel.insertText(text)
                showTextField = false
            }) {
                Text("Confirm")
            }
        } else {
            Button(onClick = { showTextField = true }) {
                Text("Add Text")
            }
        }
    }
}

@Composable
fun RatingComponent() {
    // 用于记录用户评分的变量
    val rating = remember { mutableStateOf(3) }

    Row {
        repeat(5) { index ->
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = if (index < rating.value) Color.Yellow else Color.Gray,
                modifier = Modifier
                    .clickable {
                        // 更新用户评分
                        rating.value = index + 1
                    }
                    .padding(4.dp)
            )
        }
    }
}


@Preview
@Composable
fun PreviewAddTextToDatabase() {
    // Instantiate your DbViewModel here or pass it as a parameter
    RatingComponent()
}
