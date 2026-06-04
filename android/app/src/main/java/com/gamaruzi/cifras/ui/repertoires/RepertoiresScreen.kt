package com.gamaruzi.cifras.ui.repertoires

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gamaruzi.cifras.data.Repertoire
import com.gamaruzi.cifras.ui.AppState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepertoiresScreen(
    appState: AppState,
    onBack: () -> Unit,
    onOpenRepertoire: (String) -> Unit,
) {
    val repertoires by appState.repertoires.collectAsStateWithLifecycle()
    var dialogNovo by remember { mutableStateOf(false) }
    var paraRenomear by remember { mutableStateOf<Repertoire?>(null) }
    var paraExcluir by remember { mutableStateOf<Repertoire?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Repertórios", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { dialogNovo = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Novo repertório") },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (repertoires.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.QueueMusic,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(48.dp),
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Nenhum repertório ainda",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Crie um pra agrupar as cifras de um show.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp)) {
                    items(repertoires, key = { it.id }) { rep ->
                        RepertoireRow(
                            rep = rep,
                            onClick = { onOpenRepertoire(rep.id) },
                            onRenomear = { paraRenomear = rep },
                            onExcluir = { paraExcluir = rep },
                        )
                    }
                }
            }
        }
    }

    if (dialogNovo) {
        NovoRepertoireDialog(
            onDismiss = { dialogNovo = false },
            onConfirm = { nome ->
                dialogNovo = false
                appState.createRepertoire(nome) { id -> onOpenRepertoire(id) }
            },
        )
    }

    paraRenomear?.let { rep ->
        RenomearRepertoireDialog(
            nomeAtual = rep.name,
            onDismiss = { paraRenomear = null },
            onConfirm = { novoNome ->
                appState.renameRepertoire(rep.id, novoNome)
                paraRenomear = null
            },
        )
    }

    paraExcluir?.let { rep ->
        AlertDialog(
            onDismissRequest = { paraExcluir = null },
            title = { Text("Excluir repertório?") },
            text = {
                Text(
                    "\"${rep.name}\" será removido. As cifras seguem na biblioteca.",
                    fontSize = 14.sp,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    appState.deleteRepertoire(rep.id)
                    paraExcluir = null
                }) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { paraExcluir = null }) { Text("Cancelar") }
            },
        )
    }
}

@Composable
private fun RepertoireRow(
    rep: Repertoire,
    onClick: () -> Unit,
    onRenomear: () -> Unit,
    onExcluir: () -> Unit,
) {
    var overflow by remember { mutableStateOf(false) }
    Surface(onClick = onClick, color = androidx.compose.ui.graphics.Color.Transparent) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.QueueMusic,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.size(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    rep.name,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    if (rep.songIds.isEmpty()) "Vazio"
                    else "${rep.songIds.size} ${if (rep.songIds.size == 1) "cifra" else "cifras"}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Box {
                IconButton(
                    onClick = { overflow = true },
                    modifier = Modifier.size(44.dp).clip(CircleShape),
                ) {
                    Icon(
                        Icons.Filled.MoreVert,
                        contentDescription = "Opções",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp),
                    )
                }
                DropdownMenu(expanded = overflow, onDismissRequest = { overflow = false }) {
                    DropdownMenuItem(
                        text = { Text("Renomear") },
                        leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                        onClick = { overflow = false; onRenomear() },
                    )
                    DropdownMenuItem(
                        text = { Text("Excluir") },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                            )
                        },
                        onClick = { overflow = false; onExcluir() },
                    )
                }
            }
        }
    }
}

@Composable
private fun NovoRepertoireDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var texto by remember { mutableStateOf("") }
    val fr = remember { FocusRequester() }
    LaunchedEffect(Unit) { fr.requestFocus() }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo repertório") },
        text = {
            OutlinedTextField(
                value = texto,
                onValueChange = { texto = it },
                label = { Text("Nome") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().focusRequester(fr),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(texto.trim()) },
                enabled = texto.trim().isNotEmpty(),
            ) { Text("Criar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}

@Composable
private fun RenomearRepertoireDialog(
    nomeAtual: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var texto by remember(nomeAtual) {
        mutableStateOf(TextFieldValue(nomeAtual, TextRange(0, nomeAtual.length)))
    }
    val fr = remember { FocusRequester() }
    LaunchedEffect(Unit) { fr.requestFocus() }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Renomear repertório") },
        text = {
            OutlinedTextField(
                value = texto,
                onValueChange = { texto = it },
                label = { Text("Nome") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().focusRequester(fr),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(texto.text.trim()) },
                enabled = texto.text.trim().isNotEmpty() && texto.text.trim() != nomeAtual,
            ) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}
