package com.gamaruzi.cifras.ui.settings

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gamaruzi.cifras.BuildConfig
import com.gamaruzi.cifras.data.SongFormat
import com.gamaruzi.cifras.ui.AppState
import com.gamaruzi.cifras.ui.theme.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(appState: AppState, onBack: () -> Unit) {
    val library by appState.library.collectAsStateWithLifecycle()
    val themeMode by appState.themeMode.collectAsStateWithLifecycle()
    val dynamicColor by appState.dynamicColor.collectAsStateWithLifecycle()
    var confirmarLimpar by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurações", fontSize = 20.sp, fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            val totalImg = library.count { it.format == SongFormat.IMAGE }
            val totalPdf = library.count { it.format == SongFormat.PDF }
            val totalTxt = library.count { it.format == SongFormat.TEXT }

            SecaoTitulo("Biblioteca")
            Cartao {
                Linha(Icons.Filled.Image, "Imagens", "$totalImg")
                Linha(Icons.Filled.PictureAsPdf, "PDFs", "$totalPdf")
                Linha(Icons.Filled.Description, "Textos", "$totalTxt")
                Spacer(Modifier.height(4.dp))
                Text(
                    "Para remover ou renomear uma cifra, use o menu (⋮) na lista da Busca.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 8.dp),
                )
                LinhaAcao(
                    icone = Icons.Filled.DeleteSweep,
                    titulo = "Limpar biblioteca",
                    destrutivo = true,
                    enabled = library.isNotEmpty(),
                    onClick = { confirmarLimpar = true },
                )
            }

            Spacer(Modifier.height(20.dp))
            SecaoTitulo("Aparência")
            Cartao {
                Text(
                    "Tema",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 8.dp),
                )
                SegmentedTema(
                    selecionado = themeMode,
                    onSelect = appState::setThemeMode,
                )
                Spacer(Modifier.height(16.dp))
                LinhaToggle(
                    titulo = "Cores dinâmicas (Material You)",
                    subtitulo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                        "Usa a paleta do papel de parede. Sobrepõe o verde do Tap Cifras."
                    else
                        "Disponível só no Android 12+. Mantendo verde padrão.",
                    checked = dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
                    enabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
                    onCheckedChange = appState::setDynamicColor,
                )
            }

            Spacer(Modifier.height(20.dp))
            SecaoTitulo("Privacidade")
            Cartao {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Outlined.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp).padding(top = 2.dp),
                    )
                    Spacer(Modifier.size(12.dp))
                    Column {
                        Text(
                            "Tudo fica no seu celular",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Este app não usa internet, não exige login, não envia dados pra " +
                                "lugar nenhum. As cifras que você adiciona são acessadas no " +
                                "armazenamento local via permissão direta de cada arquivo.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp,
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            SecaoTitulo("Sobre")
            Cartao {
                Linha(
                    Icons.Filled.Info,
                    "Versão",
                    BuildConfig.VERSION_NAME,
                )
            }

            Spacer(Modifier.height(40.dp))
        }
    }

    if (confirmarLimpar) {
        AlertDialog(
            onDismissRequest = { confirmarLimpar = false },
            title = { Text("Limpar biblioteca?") },
            text = {
                Text(
                    "Todas as cifras adicionadas serão removidas do Tap Cifras. " +
                        "Os arquivos originais no seu celular não serão apagados.",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    appState.clearLibrary()
                    confirmarLimpar = false
                }) {
                    Text("Limpar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarLimpar = false }) { Text("Cancelar") }
            },
        )
    }
}

@Composable
private fun SecaoTitulo(titulo: String) {
    Text(
        text = titulo.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 10.dp),
    )
}

@Composable
private fun Cartao(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
private fun Linha(icone: ImageVector, titulo: String, valor: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            icone,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.size(14.dp))
        Text(
            titulo,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Text(
            valor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun LinhaAcao(
    icone: ImageVector,
    titulo: String,
    destrutivo: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val cor = if (!enabled) MaterialTheme.colorScheme.onSurfaceVariant
              else if (destrutivo) MaterialTheme.colorScheme.error
              else MaterialTheme.colorScheme.primary
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        color = androidx.compose.ui.graphics.Color.Transparent,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icone, contentDescription = null, tint = cor, modifier = Modifier.size(22.dp))
            Spacer(Modifier.size(14.dp))
            Text(titulo, fontSize = 15.sp, color = cor)
        }
    }
}

@Composable
private fun LinhaToggle(
    titulo: String,
    subtitulo: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                titulo,
                fontSize = 15.sp,
                color = if (enabled) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                subtitulo,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp,
            )
        }
        Spacer(Modifier.size(8.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SegmentedTema(
    selecionado: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
) {
    val opcoes = listOf(
        Triple(ThemeMode.SISTEMA, "Sistema", Icons.Filled.PhoneAndroid),
        Triple(ThemeMode.CLARO, "Claro", Icons.Filled.LightMode),
        Triple(ThemeMode.ESCURO, "Escuro", Icons.Filled.DarkMode),
    )
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        opcoes.forEachIndexed { index, (mode, label, icone) ->
            SegmentedButton(
                selected = mode == selecionado,
                onClick = { onSelect(mode) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = opcoes.size),
                icon = { Icon(icone, contentDescription = null, modifier = Modifier.size(18.dp)) },
                label = { Text(label, fontSize = 13.sp) },
            )
        }
    }
}
