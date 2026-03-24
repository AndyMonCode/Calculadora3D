package com.example.calculadora3d


import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.ColorFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var isDarkTheme by remember { mutableStateOf(true) }
            var showSplashScreen by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                delay(2000)
                showSplashScreen = false
            }

            // Definición de colores
            val colors = if (isDarkTheme) {
                darkColorScheme(
                    primary = Color(0xFFD0BCFF),
                    onPrimary = Color(0xFF381E72),
                    primaryContainer = Color(0xFF4F378B),
                    onPrimaryContainer = Color(0xFFEADDFF),
                    background = Color(0xFF1C1B1F),
                    surface = Color(0xFF1C1B1F),
                    onBackground = Color(0xFFE6E1E5),
                    onSurface = Color(0xFFE6E1E5)
                )
            } else {
                lightColorScheme(
                    primary = Color(0xFF6750A4),
                    onPrimary = Color.White,
                    primaryContainer = Color(0xFFEADDFF),
                    onPrimaryContainer = Color(0xFF21005D),
                    background = Color(0xFFFFFBFE),
                    surface = Color(0xFFFFFBFE),
                    onBackground = Color(0xFF1C1B1F),
                    onSurface = Color(0xFF1C1B1F)
                )
            }

            MaterialTheme(colorScheme = colors) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Crossfade(targetState = showSplashScreen, animationSpec = tween(700), label = "Splash") { isSplash ->
                        if (isSplash) {
                            PantallaCarga()
                        } else {
                            CalculadoraScreen(
                                isDarkTheme = isDarkTheme,
                                onThemeChange = { isDarkTheme = it }
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- PANTALLA DE CARGA ---
@Composable
fun PantallaCarga() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "💰", fontSize = 80.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Calculadora 3D",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Desarrollado por",
            fontSize = 16.sp,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {

            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "AndyMonCode",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(modifier = Modifier.height(64.dp))
        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculadoraScreen(
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    // --- ESTADO MODO DE IMPRESIÓN ---
    var isResinMode by remember { mutableStateOf(false) }

    // --- VARIABLES FDM (Filamento) ---
    var precioBobina by remember { mutableStateOf("") }
    var pesoBobina by remember { mutableStateOf("1000") }
    var gramosUsados by remember { mutableStateOf("") }

    // --- VARIABLES SLA (Resina) ---
    var precioBotella by remember { mutableStateOf("") }
    var capacidadBotella by remember { mutableStateOf("1000") } // Usualmente 1000ml o 1000g
    var resinaUsada by remember { mutableStateOf("") } // ml o gramos
    var costeLavadoCurado by remember { mutableStateOf("0.5") } // Guantes, IPA, FEP...

    // --- VARIABLES COMUNES ---
    var horasImpresion by remember { mutableStateOf("") }
    var costeKwh by remember { mutableStateOf("0.15") }
    var potenciaImpresora by remember { mutableStateOf(if (isResinMode) "50" else "300") } // Resina suele consumir menos
    var horasDiseno by remember { mutableStateOf("0") }
    var precioHoraDiseno by remember { mutableStateOf("0") }
    var margenBeneficio by remember { mutableStateOf("20") }

    // --- CÁLCULOS ---
    val hImpresion = horasImpresion.toDoubleOrNull() ?: 0.0
    val cKwh = costeKwh.toDoubleOrNull() ?: 0.0
    val potImpresora = potenciaImpresora.toDoubleOrNull() ?: 0.0
    val hDiseno = horasDiseno.toDoubleOrNull() ?: 0.0
    val pHoraDiseno = precioHoraDiseno.toDoubleOrNull() ?: 0.0
    val margen = margenBeneficio.toDoubleOrNull() ?: 0.0

    // Lógica de coste de material dependiente del modo
    val costeMaterial = if (!isResinMode) {
        val pBobina = precioBobina.toDoubleOrNull() ?: 0.0
        val wBobina = pesoBobina.toDoubleOrNull() ?: 1.0
        val gUsados = gramosUsados.toDoubleOrNull() ?: 0.0
        (pBobina / wBobina) * gUsados
    } else {
        val pBotella = precioBotella.toDoubleOrNull() ?: 0.0
        val capBotella = capacidadBotella.toDoubleOrNull() ?: 1.0
        val uResina = resinaUsada.toDoubleOrNull() ?: 0.0
        val extra = costeLavadoCurado.toDoubleOrNull() ?: 0.0
        ((pBotella / capBotella) * uResina) + extra
    }

    val costeLuz = (potImpresora / 1000) * hImpresion * cKwh
    val costeManoObra = hDiseno * pHoraDiseno
    val costeTotal = costeMaterial + costeLuz + costeManoObra

    // Lógica de venta + IVA
    val precioVentaBase = costeTotal * (1 + (margen / 100))
    val iva = precioVentaBase * 0.21
    val precioFinal = precioVentaBase + iva

    // Formateador
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "ES"))

    // --- INTERFAZ ---
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // CABECERA
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "💰 Calculadora 3D",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isDarkTheme) "Oscuro" else "Claro",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = { onThemeChange(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- SELECTOR DE MODO (TABS) ---
            TabRow(
                selectedTabIndex = if (isResinMode) 1 else 0,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[if (isResinMode) 1 else 0]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                Tab(
                    selected = !isResinMode,
                    onClick = {
                        isResinMode = false
                        // Opcional: Ajustar potencia típica por defecto al cambiar
                        potenciaImpresora = "300"
                    },
                    text = { Text("Filamento (FDM)", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = isResinMode,
                    onClick = {
                        isResinMode = true
                        // Opcional: Ajustar potencia típica por defecto al cambiar
                        potenciaImpresora = "50"
                    },
                    text = { Text("Resina (SLA)", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- BLOQUE 1: MATERIAL (Dinámico) ---
            if (!isResinMode) {
                // Interfaz para Filamento
                SectionHeader(title = "1. Material (FDM)", icon = "🧵")
                InputNumber(value = precioBobina, onValueChange = { precioBobina = it }, label = "Precio Bobina (€)")
                InputNumber(value = pesoBobina, onValueChange = { pesoBobina = it }, label = "Peso Bobina (gramos)")
                InputNumber(value = gramosUsados, onValueChange = { gramosUsados = it }, label = "Gramos a usar en pieza")
            } else {
                // Interfaz para Resina
                SectionHeader(title = "1. Material (SLA)", icon = "🧪")
                InputNumber(value = precioBotella, onValueChange = { precioBotella = it }, label = "Precio Botella (€)")
                InputNumber(value = capacidadBotella, onValueChange = { capacidadBotella = it }, label = "Capacidad Botella (ml o g)")
                InputNumber(value = resinaUsada, onValueChange = { resinaUsada = it }, label = "Cantidad a usar (ml o g)")
                InputNumber(value = costeLavadoCurado, onValueChange = { costeLavadoCurado = it }, label = "Coste extra Lavado/FEP (€)")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- BLOQUE 2: ELECTRICIDAD ---
            SectionHeader(title = "2. Energía y Tiempo", icon = "⚡")
            InputNumber(value = horasImpresion, onValueChange = { horasImpresion = it }, label = "Horas de impresión")
            InputNumber(value = costeKwh, onValueChange = { costeKwh = it }, label = "Coste electricidad (€/kWh)")
            InputNumber(value = potenciaImpresora, onValueChange = { potenciaImpresora = it }, label = "Potencia Impresora (Watts)")

            Spacer(modifier = Modifier.height(16.dp))

            // --- BLOQUE 3: DISEÑO ---
            SectionHeader(title = "3. Mano de Obra", icon = "🛠️")
            InputNumber(value = horasDiseno, onValueChange = { horasDiseno = it }, label = "Horas de diseño/Post procesado")
            InputNumber(value = precioHoraDiseno, onValueChange = { precioHoraDiseno = it }, label = "Precio por hora (€)")

            Spacer(modifier = Modifier.height(16.dp))

            // --- BLOQUE 4: BENEFICIO ---
            SectionHeader(title = "4. Beneficio", icon = "📈")
            InputNumber(value = margenBeneficio, onValueChange = { margenBeneficio = it }, label = "Margen de ganancia (%)")


            Spacer(modifier = Modifier.height(24.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(16.dp))

            // --- RESULTADOS ---
            ResultRow(label = "Coste Material:", value = currencyFormat.format(costeMaterial))
            ResultRow(label = "Coste Luz:", value = currencyFormat.format(costeLuz))
            ResultRow(label = "Coste Mano Obra:", value = currencyFormat.format(costeManoObra))

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Coste Producción: ${currencyFormat.format(costeTotal)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(20.dp))

            // --- TARJETA FINAL ---
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Base Imponible:", color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(currencyFormat.format(precioVentaBase), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("IVA (21%):", color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(currencyFormat.format(iva), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("PRECIO FINAL (PVP)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                    Text(
                        text = currencyFormat.format(precioFinal),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.height(50.dp))
        }
    }
}

// --- COMPONENTE DE CABECERA ---
@Composable
fun SectionHeader(title: String, icon: String) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 18.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun InputNumber(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            if (input.all { char -> char.isDigit() || char == '.' } && input.count { it == '.' } <= 1) {
                onValueChange(input)
            }
        },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        ),
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
fun ResultRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}