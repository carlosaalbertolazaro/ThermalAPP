# Informe del proyecto: Simulador de Disipación Térmica para CPU

| | |
|---|---|
| **Nombre completo** | Carlos Alberto Lazaro Aguilar |
| **Matrícula** | 253457 |
| **Asignatura** | Desarrollo de Aplicaciones Móviles |
| **Programa** | Ingeniería en Tecnologías de la Información e Innovación Digital, Universidad Politécnica de Chiapas |
| **Periodo** | Septiembre – diciembre 2026 (Unidad I) |
| **Repositorio** | https://github.com/carlosaalbertolazaro/ThermalAPP |

---

## 1. De qué trata

La app responde una pregunta concreta: si pongo este procesador con este disipador y esta pasta térmica, ¿a qué temperatura va a trabajar? El usuario mueve dos sliders (consumo del CPU y temperatura ambiente), elige disipador y pasta con chips, activa o no el overclock, y la pantalla se actualiza en el momento.

Está hecha en Kotlin con Jetpack Compose. Sigue el patrón MVVM y un flujo unidireccional de datos. No usa hardware del teléfono, base de datos ni internet, como pedía la práctica.

## 2. Qué me propuse

El objetivo general era construir una herramienta de cálculo donde toda la lógica viviera en el ViewModel y la interfaz se limitara a mostrar el resultado.

En concreto:

- Calcular la temperatura de un CPU con una fórmula de resistencia térmica.
- Guardar el estado en una `data class` inmutable y exponerlo con `StateFlow`.
- Que los Composables no tomen decisiones de negocio.
- Extraer componentes reutilizables y sin estado propio.
- Llevar el avance en commits pequeños, no en uno solo al final.

## 3. Restricciones de la práctica y cómo se cumplen

| Restricción | Qué hice |
|---|---|
| Sin hardware ni persistencia | No hay cámara, GPS, Bluetooth, acelerómetro, Room, SharedPreferences, DataStore ni llamadas de red. |
| Lógica en memoria | Toda la matemática y las reglas están en `ThermalViewModel`. Los datos simulados son enums fijos. |
| Interfaz reactiva | Sliders, switch, chips y una tarjeta que cambia de color y de texto con cada cambio. |
| Historial de commits | El repositorio tiene 7 commits. |

## 4. Qué entra y qué sale

**Entradas**

| Entrada | Componente | Rango u opciones |
|---|---|---|
| Consumo del CPU (TDP) | Slider | 65 a 250 W |
| Temperatura ambiente | Slider | 15 a 40 °C |
| Disipador | Chips | Stock, torre de aire, AIO 240 mm |
| Pasta térmica | Chips | Genérica, pad PTM7950, metal líquido |
| Overclock | Switch | Encendido o apagado |

**Salidas**

- La temperatura final en °C.
- Un estado: segura, elevada o *thermal throttling*. La tarjeta pasa de verde a amarillo y a rojo según el caso.
- El porcentaje de rendimiento que se pierde cuando el procesador se estrangula.

## 5. La matemática

Son cinco pasos y todos viven en la función `calculate()` del ViewModel.

**Potencia efectiva.** Sin overclock es el TDP tal cual. Con overclock se multiplica por 1.25.

```
P = TDP            (overclock apagado)
P = TDP × 1.25     (overclock encendido)
```

**Resistencia térmica total.** Es lo que se opone a que el calor salga: la del disipador más la de la pasta.

```
R_total = R_disipador + R_pasta
```

**Temperatura final.**

```
T_final = T_ambiente + P × R_total
```

**Estado térmico.** Se decide por umbrales.

| Condición | Estado |
|---|---|
| T_final > 90 °C | DANGER (thermal throttling) |
| 70 °C ≤ T_final ≤ 90 °C | WARNING |
| T_final < 70 °C | SAFE |

**Pérdida de rendimiento.** Solo existe si se pasa de 90 °C. Se pierden 2 puntos porcentuales por cada grado de más, con tope de 50.

```
pérdida (%) = (T_final − 90) × 2        limitada entre 0 y 50
```

Las resistencias que usé son valores simulados, en °C/W:

| Disipador | R | Pasta térmica | R |
|---|---|---|---|
| Stock | 0.45 | Genérica | 0.10 |
| Torre de aire | 0.25 | Pad PTM7950 | 0.04 |
| AIO 240 mm | 0.15 | Metal líquido | 0.01 |

El factor de overclock (1.25), los umbrales (70 y 90), los 2 puntos por grado y el tope de 50 % están declarados como constantes con nombre en un `companion object`.

## 6. Cómo está armada

La separación es simple: el ViewModel decide y la pantalla pinta.

```
Usuario ──► Composable ──(lambda)──► ViewModel ──► _uiState (MutableStateFlow)
   ▲                                                        │
   │                                                        ▼
   └────── recomposición ◄── collectAsStateWithLifecycle ◄── uiState (StateFlow)
```

- **`data`:** `Cooler` y `ThermalPaste` son enums. Cada opción lleva su nombre (como referencia a `strings.xml`) y su resistencia térmica.
- **`ThermalUiState`:** una `data class` con solo `val`. Guarda lo que eligió el usuario y lo que se calculó. Para cambiar algo se usa `copy()`, así que cada cambio produce un objeto nuevo y Compose sabe que debe redibujar.
- **`ThermalViewModel`:** tiene `_uiState` privado y mutable, y `uiState` público de solo lectura (Backing Property). Cada acción del usuario entra por una función `on...`, que recalcula y publica el estado nuevo.
- **`ThermalScreen` y los componentes:** reciben el estado y lambdas. No saben que existe un ViewModel (State Hoisting).
- **`MainActivity`:** pide el ViewModel, lee el estado con `collectAsStateWithLifecycle()` y conecta cada lambda de la pantalla con su función del ViewModel. No hay `mutableStateOf` ahí.

Como el estado está en el ViewModel y no en la pantalla, no se pierde al rotar el dispositivo.

**Estructura de paquetes** (`com.upchiapas.kt_template`)

```
data/               Cooler.kt, ThermalPaste.kt
ui/                 ThermalStatus.kt, ThermalUiState.kt, ThermalViewModel.kt, ThermalScreen.kt
ui/components/      LabeledSlider.kt, OptionChips.kt
ui/theme/           Color.kt, Theme.kt, Type.kt (vienen del template)
MainActivity.kt
res/values/strings.xml    todos los textos visibles
```

## 7. Qué pasa cuando se mueve un slider

Tomo el de TDP como ejemplo.

1. El `Slider` dentro de `LabeledSlider` detecta el arrastre y llama a `onValueChange` con el valor nuevo.
2. Esa lambda es `onTdpChange`, que `MainActivity` enlazó con `viewModel::onTdpChange`.
3. El ViewModel ejecuta `_uiState.update { calculate(it.copy(tdpWatts = value)) }`. Aquí `it` es el estado actual.
4. `calculate()` rehace todo: potencia, resistencia, temperatura, estado y pérdida.
5. `update` guarda el estado nuevo. Es distinto del anterior, así que `StateFlow` emite.
6. `collectAsStateWithLifecycle()` convierte esa emisión en estado de Compose.
7. Compose vuelve a ejecutar `ThermalScreen` con los datos nuevos y se redibuja lo que cambió.

Los chips y el switch siguen el mismo camino. Solo cambia la función `on...` que se llama.

## 8. Componentes de interfaz

- **`ThermalScreen`:** la pantalla completa. Recibe un `ThermalUiState` y las lambdas de eventos.
- **`LabeledSlider`:** un texto con un slider. Se usa dos veces, para TDP y para temperatura ambiente. No guarda nada: muestra el valor que le llega y avisa cuando cambia.
- **`OptionChips<T>`:** una fila de `FilterChip`. Es genérico, así que el mismo componente sirve para elegir `Cooler` y `ThermalPaste`. Recibe las opciones, cuál está elegida y qué hacer al tocar una.
- Del resto se encarga Material 3: `Text`, `Card`, `Slider`, `FilterChip`, `Switch`, `Column` y `Row`, con la tipografía de `MaterialTheme`.
- El color de la tarjeta depende del estado térmico, y ningún texto está escrito en el código: todos salen de `strings.xml`.

## 9. Pruebas con números

Calculé estos casos a mano antes de compararlos con la app.

| TDP | Ambiente | Disipador | Pasta | OC | T_final | Estado | Pérdida |
|---|---|---|---|---|---|---|---|
| 150 W | 25 °C | Torre | Genérica | No | 77.5 °C | WARNING | 0 % |
| 142 W | 35 °C | Torre | Genérica | No | 84.7 °C | WARNING | 0 % |
| 150 W | 25 °C | Torre | Genérica | Sí | 90.6 °C | DANGER | 1 % |
| 250 W | 25 °C | AIO 240 | Metal líquido | No | 65.0 °C | SAFE | 0 % |
| 250 W | 25 °C | Stock | Genérica | No | 162.5 °C | DANGER | 50 % (tope) |

El último caso no es realista: ningún CPU llega a 162 °C sin apagarse. Lo dejé porque comprueba que el tope de 50 % funciona.

## 10. Rúbrica, punto por punto

| Indicador | Dónde se ve |
|---|---|
| Complejidad | Cinco entradas que se combinan con condicionales y fórmulas encadenadas. |
| Riqueza visual | Siete tipos de Composables, color de la tarjeta según el estado y tipografía del tema. |
| Gestión de estados | `StateFlow` con una `data class` inmutable, `collectAsStateWithLifecycle()` y flujo unidireccional. |
| Composables reutilizables | `LabeledSlider` y `OptionChips<T>`, sin estado propio. |
| Patrón MVVM | Backing Property y toda la lógica en el ViewModel. |
| Calidad del código | Paquetes por responsabilidad, nombres según las convenciones de Kotlin, textos en `strings.xml`. |
| Defensa oral | El recorrido de un evento está en la sección 7. |

## 11. Historial de commits

Fui avanzando por fases y subiendo cada una: dependencias y datos simulados, `UiState`, `ViewModel` con el cálculo, pantalla principal y, por último, los componentes reutilizables. El repositorio tiene 7 commits.

## 12. Cómo correrla

**Desde el código**

1. `git clone https://github.com/carlosaalbertolazaro/ThermalAPP.git`
2. Abrir la carpeta en Android Studio y esperar el Gradle Sync.
3. Ejecutar el módulo `app` en un emulador o en un teléfono con Android 8.0 (API 26) o superior.

**Desde el APK**

1. Bajar el `.apk` de la sección *Releases* del repositorio.
2. Pasarlo al teléfono y abrirlo. Android va a pedir permiso para instalar desde fuentes desconocidas.

## 13. Lo que quedó fuera

El modelo es una simplificación. No toma en cuenta el flujo de aire de la caja ni cómo cambia la carga con el tiempo.

Por tiempo no alcancé a hacer varias cosas que tenía pensadas:

- Animar el color de la tarjeta y la alerta de throttling (`animateColorAsState` y `AnimatedVisibility`).
- Dibujar un termómetro con `Canvas`.
- Mostrar un ranking de las 9 combinaciones de disipador y pasta.
- Pruebas unitarias del ViewModel.

## 14. Conclusiones

El resultado cumple lo que pedía la práctica. La lógica está en un solo lugar, la pantalla no decide nada y los dos componentes se reutilizan.

Lo que más pesó en el diseño fue separar el estado inmutable, el ViewModel y los Composables. Con eso, cambiar una regla de negocio (un umbral, el factor de overclock, una resistencia) es editar un valor y no revisar la interfaz.

