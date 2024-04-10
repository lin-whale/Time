package com.example.time.ui.timeRecord

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.time.logic.model.LifePiece

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LifeList(lifeList: List<LifePiece>, onLifeSelected: (LifePiece) -> Unit) {
    FlowRow() {
        lifeList.forEach { lifePiece ->
            LifeButton(lifePiece, onLifeSelected)
        }
    }
}

@Composable
fun LifeButton(lifePiece: LifePiece, onLifeSelected: (LifePiece) -> Unit) {
    Button(
        onClick = { onLifeSelected(lifePiece) },
        modifier = Modifier
            .padding(0.dp)
            .wrapContentSize()
    ) {
        Text(
            text = lifePiece.lifePiece,
            modifier = Modifier
                .wrapContentSize()
                .padding(0.dp)
        )
    }
}