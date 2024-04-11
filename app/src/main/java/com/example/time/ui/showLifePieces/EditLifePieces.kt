package com.example.time.ui.showLifePieces

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.time.logic.model.LifePiece
import com.example.time.ui.TimeViewModel

@Composable
fun LifePieceListEdit(lifePieceList: List<LifePiece>, viewModel: TimeViewModel) {
    LazyColumn {
        items(lifePieceList) { lifePiece ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(8.dp)
            ) {
                Text(text = lifePiece.lifePiece, modifier = Modifier.weight(1f))
                IconButton(onClick = { viewModel.deleteLifePiece(lifePiece.lifePiece) }) {
                    Icon(Icons.Default.Clear, contentDescription = "Delete")
                }
            }
        }
    }
}