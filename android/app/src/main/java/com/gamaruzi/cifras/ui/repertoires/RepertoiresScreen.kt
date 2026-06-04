package com.gamaruzi.cifras.ui.repertoires

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gamaruzi.cifras.data.Repertoire
import com.gamaruzi.cifras.ui.AppState
import com.gamaruzi.cifras.ui.common.DEFAULT_ENTITY_COLOR_KEY
import com.gamaruzi.cifras.ui.common.EntityColorPalette
import com.gamaruzi.cifras.ui.common.entityColorByKey

// Mesmo limite usado em pastas, pra manter a lista alinhada e o card do
// repertório com nome legível sem ellipsis agressivo.
internal const val REPERTOIRE_NAME_MAX_LEN = 20

// Ordenação local da tela de repertórios (separada da home pra não
// poluir SearchScreen.SortMode). Default = A→Z.
private enum class RepSort(val label: String) {
    ALFABETICA_ASC("A → Z"),
    ALFABETICA_DESC("Z → A"),
    QUANTIDADE_DESC("Maior repertório"),
    QUANTIDADE_ASC("Menor repertório");
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepertoiresScreen(
    appState: AppState,
    onBack: () -> Unit,
    onOpenRepertoire: (String) -> Unit,
) {
    val repertoires by appState.repertoires.collectAsStateWithLifecycle()
    var dialogNovo by remember { mutableStateOf(false) }
    var paraEditar by remember { mutableStateOf<Repertoire?>(null) }
    var paraExcluir by remember { mutableStateOf<Repertoire?>(null) }
    var sortAtual by remember { mutableStateOf(RepSort.ALFABETICA_ASC) }

    val ordenados = remember(repertoires, sortAtual) {
        when (sortAtual) {
            RepSort.ALFABETICA_ASC -> repertoires.sortedBy { it.name.lowercase() }
            RepSort.ALFABETICA_DESC -> repertoires.sortedByDescending { it.name.lowercase() }
            RepSort.QUANTIDADE_DESC -> repertoires.sortedWith(
                compareByDescending<Repertoire> { it.songIds.size }.thenBy { it.name.lowercase() }
            )
            RepSort.QUANTIDADE_ASC -> repertoires.sortedWith(
                compareBy<Repertoire> { it.songIds.size }.thenBy { it.name.lowercase() }
            )
        }
    }

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
            // FAB só aparece quando a lista não está vazia; no estado vazio
            // o CTA fica inline com a ilustração, mais convidativo.
            if (repertoires.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { dialogNovo = true },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Novo repertório") },
                )
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (repertoires.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(64.dp),
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Nenhum repertório ainda",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Monte uma sequência de cifras pra abrir tudo de uma vez no palco.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.widthIn(max = 320.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                    Spacer(Modifier.height(28.dp))
                    Button(onClick = { dialogNovo = true }) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(Modifier.size(8.dp))
                        Text("Criar repertório", fontSize = 15.sp)
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 6.dp)) {
                        SortDropdown(
                            atual = sortAtual,
                            onSelect = { sortAtual = it },
                        )
                    }
                    LazyColumn(contentPadding = PaddingValues(top = 4.dp, bottom = 96.dp)) {
                        items(ordenados, key = { it.id }) { rep ->
                            RepertoireRow(
                                rep = rep,
                                onClick = { onOpenRepertoire(rep.id) },
                                onEditar = { paraEditar = rep },
                                onExcluir = { paraExcluir = rep },
                            )
                        }
                    }
                }
            }
        }
    }

    if (dialogNovo) {
        RepertoireDialog(
            titulo = "Novo repertório",
            nomeInicial = "",
            corInicial = DEFAULT_ENTITY_COLOR_KEY,
            confirmLabel = "Criar",
            onDismiss = { dialogNovo = false },
            onConfirm = { nome, cor ->
                dialogNovo = false
                appState.createRepertoire(name = nome, color = cor) { id -> onOpenRepertoire(id) }
            },
        )
    }

    paraEditar?.let { rep ->
        RepertoireDialog(
            titulo = "Editar repertório",
            nomeInicial = rep.name,
            corInicial = rep.color,
            confirmLabel = "Salvar",
            onDismiss = { paraEditar = null },
            onConfirm = { novoNome, novaCor ->
                appState.renameRepertoire(rep.id, novoNome, novaCor)
                paraEditar = null
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
private fun SortDropdown(
    atual: RepSort,
    onSelect: (RepSort) -> Unit,
) {
    var aberto by remember { mutableStateOf(false) }
    Surface(
        onClick = { aberto = true },
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Sort,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.size(6.dp))
            Text(
                "Ordenar: ${atual.label}",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Icon(
                Icons.Filled.ArrowDropDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        }
    }
    DropdownMenu(expanded = aberto, onDismissRequest = { aberto = false }) {
        RepSort.entries.forEach { mode ->
            DropdownMenuItem(
                text = { Text(mode.label) },
                trailingIcon = if (mode == atual) {
                    { Icon(Icons.Filled.Check, contentDescription = null) }
                } else null,
                onClick = { aberto = false; onSelect(mode) },
            )
        }
    }
}

@Composable
private fun RepertoireRow(
    rep: Repertoire,
    onClick: () -> Unit,
    onEditar: () -> Unit,
    onExcluir: () -> Unit,
) {
    var overflow by remember { mutableStateOf(false) }
    val cor = entityColorByKey(rep.color)
    Surface(onClick = onClick, color = Color.Transparent) {
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
                    .background(cor.container),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = cor.onContainer,
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
                        text = { Text("Editar") },
                        leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                        onClick = { overflow = false; onEditar() },
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
private fun RepertoireDialog(
    titulo: String,
    nomeInicial: String,
    corInicial: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit,
) {
    var texto by remember(nomeInicial) {
        mutableStateOf(TextFieldValue(nomeInicial, TextRange(0, nomeInicial.length)))
    }
    var corSelecionada by remember(corInicial) { mutableStateOf(corInicial) }
    val fr = remember { FocusRequester() }
    LaunchedEffect(Unit) { fr.requestFocus() }

    val textoLimpo = texto.text.trim()
    val excedeu = textoLimpo.length > REPERTOIRE_NAME_MAX_LEN
    val mudou = textoLimpo.isNotEmpty() && !excedeu &&
        (textoLimpo != nomeInicial || corSelecionada != corInicial)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titulo) },
        text = {
            Column {
                OutlinedTextField(
                    value = texto,
                    onValueChange = { novo ->
                        if (novo.text.length <= REPERTOIRE_NAME_MAX_LEN) texto = novo
                    },
                    label = { Text("Nome") },
                    singleLine = true,
                    isError = excedeu,
                    supportingText = {
                        Text(
                            "${textoLimpo.length}/$REPERTOIRE_NAME_MAX_LEN caracteres",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    modifier = Modifier.fillMaxWidth().focusRequester(fr),
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    "Cor",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                PaletaCores(selecionada = corSelecionada, onSelect = { corSelecionada = it })
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(textoLimpo, corSelecionada) },
                enabled = mudou,
            ) { Text(confirmLabel) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}

@Composable
private fun PaletaCores(
    selecionada: String,
    onSelect: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        EntityColorPalette.forEach { cor ->
            val isSelected = cor.key == selecionada
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(cor.container)
                    .border(
                        width = if (isSelected) 3.dp else 0.dp,
                        color = MaterialTheme.colorScheme.onSurface,
                        shape = CircleShape,
                    )
                    .clickable { onSelect(cor.key) },
                contentAlignment = Alignment.Center,
            ) {
                if (isSelected) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = cor.label,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}
