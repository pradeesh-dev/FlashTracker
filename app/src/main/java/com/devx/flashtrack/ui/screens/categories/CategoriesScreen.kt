package com.devx.flashtrack.ui.screens.categories

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.devx.flashtrack.data.local.entity.CategoryEntity
import com.devx.flashtrack.ui.components.iconEmojiFor
import com.devx.flashtrack.ui.theme.*
import com.devx.flashtrack.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    var showAddSheet by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var selectedTypeFilter by remember { mutableStateOf("ALL") }

    val filtered = when (selectedTypeFilter) {
        "EXPENSE" -> categories.filter { it.type in listOf("EXPENSE", "BOTH") }
        "INCOME"  -> categories.filter { it.type in listOf("INCOME", "BOTH") }
        else      -> categories
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Categories", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBackIosNew, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) { Icon(Icons.Rounded.Add, "Add Category") }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Type filter
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("ALL", "EXPENSE", "INCOME").forEach { type ->
                    FilterChip(
                        selected = selectedTypeFilter == type,
                        onClick = { selectedTypeFilter = type },
                        label = { Text(type.lowercase().replaceFirstChar { it.uppercase() },
                                       style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered, key = { it.id }) { cat ->
                    CategoryGridItem(
                        category = cat,
                        onClick = { editingCategory = cat }
                    )
                }
                item {
                    // Add new category button
                    Column(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { showAddSheet = true },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Rounded.AddCircleOutline, "Add",
                             tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.height(4.dp))
                        Text("Add", style = MaterialTheme.typography.labelSmall,
                             color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }

    if (showAddSheet || editingCategory != null) {
        AddEditCategorySheet(
            existing = editingCategory,
            onDismiss = { showAddSheet = false; editingCategory = null },
            onSave = { cat ->
                if (editingCategory != null) viewModel.updateCategory(cat) else viewModel.addCategory(cat)
                showAddSheet = false; editingCategory = null
            },
            onDelete = { editingCategory?.let { viewModel.deleteCategory(it) }; editingCategory = null }
        )
    }
}

@Composable
private fun CategoryGridItem(category: CategoryEntity, onClick: () -> Unit) {
    val catColor = try { Color(android.graphics.Color.parseColor(category.colorHex)) }
                   catch (e: Exception) { GreenIncome }
    Column(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(catColor.copy(alpha = 0.1f))
            .border(BorderStroke(1.dp, catColor.copy(alpha = 0.3f)), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(iconEmojiFor(category.iconName), fontSize = 26.sp)
        Spacer(Modifier.height(6.dp))
        Text(
            category.name,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            maxLines = 2,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            fontWeight = FontWeight.SemiBold
        )
        if (category.budget > 0) {
            Text("₹${"%,.0f".format(category.budget)}/mo",
                 style = MaterialTheme.typography.labelSmall, color = catColor)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditCategorySheet(
    existing: CategoryEntity?,
    onDismiss: () -> Unit,
    onSave: (CategoryEntity) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var budget by remember { mutableStateOf(existing?.budget?.takeIf { it > 0 }?.toString() ?: "") }
    var selectedColor by remember { mutableStateOf(existing?.colorHex ?: "#FF5722") }
    var selectedIcon by remember { mutableStateOf(existing?.iconName ?: "category") }
    var selectedType by remember { mutableStateOf(existing?.type ?: "BOTH") }

    val icons = listOf(
        "shopping_basket","restaurant","shopping_bag","receipt_long","movie","flight",
        "local_hospital","school","local_gas_station","account_balance","payments",
        "work","trending_up","home","swap_horiz","category","credit_card","account_balance_wallet"
    )
    val colors = listOf(
        "#FF5722","#4CAF50","#2196F3","#FF9800","#9C27B0","#00BCD4",
        "#E91E63","#607D8B","#FF7043","#26A69A","#AB47BC","#FDD835"
    )

    ModalBottomSheet(onDismissRequest = onDismiss, modifier = Modifier.fillMaxHeight(0.9f)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(if (existing != null) "Edit Category" else "Add Category",
                 style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Category Name") }, modifier = Modifier.fillMaxWidth(),
                singleLine = true, shape = RoundedCornerShape(12.dp)
            )

            // Type
            Text("Type", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("EXPENSE", "INCOME", "BOTH").forEach { t ->
                    FilterChip(selected = selectedType == t, onClick = { selectedType = t },
                               label = { Text(t.lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelSmall) })
                }
            }

            // Monthly budget
            OutlinedTextField(
                value = budget, onValueChange = { budget = it },
                label = { Text("Monthly Budget (optional)") }, prefix = { Text("₹") },
                modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
            )

            // Icon
            Text("Icon", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(icons) { icon ->
                    Box(
                        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp))
                            .background(if (selectedIcon == icon) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                            .border(if (selectedIcon == icon) BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                                    else BorderStroke(0.dp, Color.Transparent), RoundedCornerShape(12.dp))
                            .clickable { selectedIcon = icon },
                        contentAlignment = Alignment.Center
                    ) { Text(iconEmojiFor(icon), fontSize = 22.sp) }
                }
            }

            // Color
            Text("Color", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(colors) { hex ->
                    val c = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { GreenIncome }
                    Box(
                        modifier = Modifier.size(36.dp).clip(CircleShape)
                            .background(c)
                            .border(if (selectedColor == hex) BorderStroke(3.dp, Color.White)
                                    else BorderStroke(0.dp, Color.Transparent), CircleShape)
                            .clickable { selectedColor = hex }
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (existing != null && !existing.isDefault) {
                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, RedExpense.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RedExpense)
                    ) { Text("Delete") }
                }
                Button(
                    onClick = {
                        if (name.isBlank()) return@Button
                        onSave(CategoryEntity(
                            id = existing?.id ?: 0,
                            name = name,
                            iconName = selectedIcon,
                            colorHex = selectedColor,
                            isDefault = existing?.isDefault ?: false,
                            type = selectedType,
                            budget = budget.toDoubleOrNull() ?: 0.0
                        ))
                    },
                    modifier = Modifier.weight(if (existing != null && !existing.isDefault) 1f else 1f).height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Save", fontWeight = FontWeight.Bold) }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
