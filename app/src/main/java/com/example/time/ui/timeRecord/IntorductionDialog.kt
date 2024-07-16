package com.example.time.ui.timeRecord

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.VerticalAlignmentLine
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogWindowProvider
import com.example.time.R
import com.example.time.logic.model.LifePiece
import com.example.time.logic.utils.convertTimeFormat
import com.example.time.ui.showLifePieces.LifePieceListEdit
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun IntroductionDialog(onCancel: () -> Unit) {

    Dialog(onDismissRequest = onCancel) {
//        Box(Modifier.background(Color.White).fillMaxWidth()){
            Column(
                Modifier.background(Color.White).fillMaxWidth()
//                modifier = Modifier
////                .padding(16.dp)
//                    .graphicsLayer {
//                        this.ambientShadowColor = Color.Black
//                        shadowElevation = 600.0F
//                    }
            ) {
                MinimalExampleContent()
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
////                    modifier = Modifier.padding(8.dp)
//                ) {
//                    Button(onClick = onCancel, ) {
//                        Text("好的")
//                    }
//                }
            }
//        }
    }
}

@Composable
fun MinimalExampleContent() {
    val markdownContent = """
# 使用说明
**从上到下，分别由这几个部分构成**
- 首行： 显示上次记录的事件名称及时间
- 事件文本框(回首向来萧瑟处)： 输入你从上次记录到现在的事件名称
- 常用事件区域：可以将常用事件添加在这个区域，点击即可将该事件输入到事件文本框中
- 体验文本框(也无风雨也无晴)： 输入任何你想要写下来的东西,体验,备忘,随便写点什么都可以...
- 体验打分区： 你觉得过去这段时间体验如何？快乐还是难过，激动还是沮丧？自己评个分吧！
- 时光回溯按钮： 事件的结束时间不是现在，而是在往前一点，可以用进度条拉动选择时间点，点击确认就好啦~
- Event按钮： 可以添加常用事件到主界面，也可以进行删除~
-  ✓按钮： 确认！记录这段时间的事件。
 1. 举例，上次事件结束时间为9:00,现在是11:30，事件文本框填入了"羽毛球"，那么9:00-11.30这段时间会记录为"羽毛球"~
- ?按钮：显示帮助
- 📋, 🕒, 💖：显示关于你的一切时间碎片的统计信息，让你明白时间都去哪了~
 1. 📋：显示每一个时间碎片的详细信息
 2. 🕒：按时间长度显示，时间都去哪了~
 3. 💖：按体验得分统计，这段时间过得如何~ 在扇形图下方点击⭐1~⭐5，即可显示这个level的所有事件
 4. 上述三个统计界面，均可以在界面顶部选择时间的起点和结束点。举例，选择```2024-02-14 -> 2024-03-01```: 则仅统计```2024-02-14:00:00:00 -> 2024-03-01:00:00:00```这个时间段的所有时间碎片。
 5. 🕒及💖中，下方呈现的时间列表，每一行左边的小圆点可以点击，点击后会显示对于这个事件的统计信息，你可以看看做这件事的时候体验如何~为什么快乐，又为什么失望~

**Enjoy it~ 欢迎提出任何建议**
	"""
    MarkdownText(
        markdown = markdownContent,
        maxLines = 100,
        style = TextStyle(
            color = Color.Black,
            fontSize = 12.sp,
            lineHeight = 10.sp,
            textAlign = TextAlign.Justify,
        ),
        )
}