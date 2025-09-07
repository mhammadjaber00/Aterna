//package io.yavero.aterna.features.classselection
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.selection.selectableGroup
//import androidx.compose.material3.Surface
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import io.yavero.aterna.designsystem.theme.AternaColors
//import io.yavero.aterna.domain.model.ClassType
//
//@Composable
//fun ClassSelectionScreen(
//    selected: ClassType? = null,
//    onSelect: (ClassType) -> Unit,
//    onConfirm: (ClassType) -> Unit,
//    modifier: Modifier = Modifier
//) {
//    Surface(
//        modifier = modifier.fillMaxSize(),
//        color = AternaColors.AternaNight
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .windowInsetsPadding(WindowInsets.safeDrawing)
//                .padding(horizontal = 20.dp, vertical = 24.dp)
//                .selectableGroup(),
//            verticalArrangement = Arrangement.spacedBy(20.dp)
//        ) {
//            ClassSelectionHeader()
//
//            ClassOptions(
//                selected = selected,
//                onSelect = onSelect
//            )
//
//            Spacer(Modifier.weight(1f))
//
//            ClassSelectionFooter(
//                selected = selected,
//                onConfirm = onConfirm
//            )
//        }
//    }
//}