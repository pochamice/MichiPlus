# Registro de cambios — DisneyPlus

Este archivo solo documenta los cambios. No forma parte del código y no se compila.

Cada cambio se hizo en el `.java` y también en su `.form`. NetBeans regenera el código de
`initComponents()` a partir del `.form`, así que si solo se cambiara el `.java`, NetBeans
borraría el cambio la próxima vez que se abra el diseñador.

No se modificó la lógica: los métodos `guardarYcontinuar()`, `elegirPortada()` y `regresarPA()`,
la clase `Contenido`, los listeners, los estilos y los textos siguen igual.

---

## Cambio 1 — Ventanas maximizadas

> Esta es la primera versión. El código final está en el cambio 8.

### Pantalla2.java (constructor)

Antes:
```java
setSize(1920, 1080);
setLocationRelativeTo(null);
```
Después:
```java
setLocationRelativeTo(null);
setExtendedState(JFrame.MAXIMIZED_BOTH);
```
| Línea | Qué hace |
|---|---|
| `setLocationRelativeTo(null);` | Centra la ventana en el monitor. Se dejó como estaba. |
| `setExtendedState(JFrame.MAXIMIZED_BOTH);` | Abre la ventana maximizada a lo ancho y a lo alto. Así ocupa toda la pantalla sin importar la resolución del monitor. |
| ~~`setSize(1920, 1080);`~~ | Se quitó. Con un tamaño fijo de 1920×1080, en pantallas más pequeñas la ventana se salía del monitor. |

### Pantalla3.java (los dos constructores)

```java
public Pantalla3() {
    initComponents();
    setExtendedState(MAXIMIZED_BOTH);
}

public Pantalla3(Contenido contenido) {
    initComponents();
    setExtendedState(MAXIMIZED_BOTH);
    ...
```
| Línea | Qué hace |
|---|---|
| `setExtendedState(MAXIMIZED_BOTH);` | Lo mismo que en Pantalla2. Se puso en los dos constructores para que la ventana se maximice con cualquiera de ellos. |

---

## Cambio 2 — Centrar el contenido (Pantalla2, panel de fondo `PanelPrincipal`)

Antes, el logo y el panel blanco tenían espacios fijos a la izquierda (`addGap(562)` y `addGap(458)`),
por eso quedaban corridos hacia un lado.

```java
.addGroup(PanelPrincipalLayout.createSequentialGroup()
    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
    .addGroup(PanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
        .addComponent(iconoDisney, ...PREFERRED_SIZE, 395, ...PREFERRED_SIZE)
        .addComponent(PanelBlanco, ...PREFERRED_SIZE, ...DEFAULT_SIZE, ...PREFERRED_SIZE))
    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
```
| Línea | Qué hace |
|---|---|
| `addContainerGap(DEFAULT_SIZE, Short.MAX_VALUE)` (primera) | Espacio **flexible** a la izquierda. Puede crecer todo lo necesario. |
| `createParallelGroup(Alignment.CENTER)` | Pone el logo y el panel blanco uno debajo del otro, centrados entre sí. |
| `addComponent(iconoDisney, ...)` / `addComponent(PanelBlanco, ...)` | Los mismos componentes de antes, con el mismo tamaño. |
| `addContainerGap(DEFAULT_SIZE, Short.MAX_VALUE)` (última) | Espacio flexible a la derecha. Como los dos espacios crecen igual, el contenido queda en el centro. |

En el eje vertical se hizo lo mismo: arriba y abajo se cambiaron `addGap(33)` y `addContainerGap(265, ...)`
por espacios flexibles. Así el bloque también queda centrado de arriba a abajo.

---

## Cambio 3 — Reacomodo del panel blanco de Pantalla2 (`PanelBlanco`)

Se reemplazó todo el bloque `PanelBlancoLayout` por uno nuevo con dos columnas.

### Distribución resultante
```
            Bienvenido al gestor de peliculas y series.
     Añada los datos de la pelicula o serie que desea agregar...

 Ingrese el nombre:  [__________]      Añade la imagen enlazada:
 Ingrese el genero:  [Romance  v]          ┌──────────┐
 ¿Serie o Pelicula?  [Pelicula v]          │  póster  │
 Sinopsis:           ┌─────────┐           │ 180×270  │
                     │         │           └──────────┘
                     └─────────┘          [Elegir portada]

                          [ Añadir ]
                 ¿Algun problema? Contáctanos.
```

### Horizontal (`setHorizontalGroup`)
| Línea / parte | Qué hace |
|---|---|
| `addGap(40, 40, 40)` (al inicio y al final) | Margen de 40 px a cada lado dentro del panel blanco. |
| `createParallelGroup(Alignment.CENTER)` | Grupo principal: todo lo que contiene queda centrado horizontalmente. |
| `addComponent(LblBienvenida, ...DEFAULT_SIZE...)` | El título toma el ancho de su texto y queda centrado. |
| `addComponent(Lbl_Info, ...DEFAULT_SIZE...)` | El texto de instrucciones, igual: centrado. |
| Grupo con `LblNombre`, `LblGenero`, `LblSrsPeli`, `LblDesc` (150 px) | Columna de etiquetas, todas con el mismo ancho y alineadas a la izquierda. |
| `addGap(10, 10, 10)` | Separación de 10 px entre la etiqueta y su campo. |
| Grupo con `TxTNombre`, `ComBoxGen`, `ComBoxSrsPeli`, `jScrollPane1` (240 px) | Columna de campos, todos del mismo ancho para que queden parejos. |
| `addGap(50, 50, 50)` | Separación de 50 px entre el formulario y la columna del póster. |
| Grupo `CENTER` con `LblImagen`, `lblpreview` (180 px), `btnImagen` (180 px) | Columna del póster: el texto, el recuadro y el botón, centrados uno sobre otro. |
| `addComponent(btnAniadir, ..., 200, ...)` | Botón "Añadir" de 200 px de ancho, centrado debajo de todo. |
| `addComponent(lblinfocontacto, ...)` | Texto de contacto, centrado al final. |

### Vertical (`setVerticalGroup`)
| Línea / parte | Qué hace |
|---|---|
| `addGap(20)` → `LblBienvenida` (31) → `addGap(4)` → `Lbl_Info` (28) → `addGap(20)` | Margen superior, el título, el texto de instrucciones y un espacio antes del formulario. |
| `addGap(32, 32, 32)` (al inicio de la columna izquierda) | Baja el formulario 32 px para que empiece a la altura del recuadro del póster y no a la del texto "Añade la imagen". |
| Grupos `BASELINE` (Nombre, Género, Serie/Película), altura 32 | Cada etiqueta queda alineada con la línea del texto de su campo. |
| `addGap(18, 18, 18)` entre filas | Separación pareja de 18 px entre cada fila del formulario. |
| `LblDesc` + `jScrollPane1` (186 px) | El área de la sinopsis es más alta, para que termine a la misma altura que el botón "Elegir portada". |
| `LblImagen` (24) → `addGap(8)` → `lblpreview` (**270**) → `addGap(10)` → `btnImagen` (38) | Columna del póster de arriba hacia abajo. |
| `addGap(20)` → `btnAniadir` (50) → `addGap(8)` → `lblinfocontacto` (30) → `addGap(15)` | Botón "Añadir", texto de contacto y margen inferior. |

### Recuadro de vista previa (`lblpreview`)
- **Antes:** 189×118, más ancho que alto, que no es la forma de un póster.
- **Después:** **180×270**, proporción 2:3, la de un póster de película.
- `elegirPortada()` escala la imagen usando `lblpreview.getWidth()` y `getHeight()`, así que con el nuevo
  tamaño el póster se ajusta solo, sin tocar ese método.

```java
lblpreview.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
```
| Línea | Qué hace |
|---|---|
| `setHorizontalAlignment(SwingConstants.CENTER)` | Centra la imagen dentro del recuadro. Antes quedaba pegada a la izquierda. Es solo una propiedad visual. |

---

## Cambio 4 — Reacomodo de Pantalla3 (`panelfondo`)

### Distribución resultante
```
   [Logo Disney+]                     ┌───────────────┐
   Título                             │               │
   Género   Tipo                      │    PORTADA    │
   [REPRODUCIR] [REGRESAR]            │    440×660    │
   SINOPSIS                           │               │
   ┌──────────────────────────┐       │               │
   │ texto de la sinopsis     │       │               │
   └──────────────────────────┘       └───────────────┘
```

### Horizontal
| Línea / parte | Qué hace |
|---|---|
| `addContainerGap(DEFAULT_SIZE, Short.MAX_VALUE)` (inicio y final) | Espacios flexibles a ambos lados, para que el conjunto quede centrado. Antes había un `addGap(23)` fijo. |
| `jLabel1` (410) | El logo, con el mismo tamaño de antes. |
| `lbltitulo` (**620**, antes 383) | Título más ancho, para que quepan nombres largos. |
| `lblgenero` (145) + `UNRELATED` + `lblsrspel` (177) | Género y tipo en la misma fila, como antes. |
| `btnReproducir` (176) + `addGap(20)` + `btnRegresar` (189) | Los dos botones lado a lado, separados por 20 px (antes 26). |
| `lblsinopsis` (182) | Título "SINOPSIS", igual que antes. |
| `jScrollPane1` (**620**, antes 805) | Área de la sinopsis, del mismo ancho que el título para que la columna quede pareja. |
| `addGap(80, 80, 80)` | Separación fija de 80 px entre la información y la portada. Antes era un espacio de 238 px que además se estiraba. |
| `lblPortada` (**440**, antes 754) | Recuadro de la portada, ahora con ancho de póster. |

### Vertical
| Línea / parte | Qué hace |
|---|---|
| `addContainerGap(DEFAULT_SIZE, Short.MAX_VALUE)` (arriba y abajo) | Centra el contenido de arriba a abajo. Antes había `addGap(36)` arriba y `315` abajo. |
| `createParallelGroup(Alignment.CENTER)` | La columna de información y la portada quedan centradas entre sí en vertical. |
| `lblPortada` (**660**, antes 706) | Alto del póster. Con el ancho de 440 da la proporción 2:3. |
| `jLabel1` (197) → `addGap(18)` → `lbltitulo` (60) → `addGap(12)` | Logo y título. El título bajó de 79 a 60 de alto para compactar la columna. |
| Género/tipo (40) → `addGap(24)` → botones (48) → `addGap(20)` | Fila de género/tipo y fila de botones. |
| `lblsinopsis` (30) → `RELATED` → `jScrollPane1` (200) | Título "SINOPSIS" y su área de texto, más compactos (antes 50 y 235). |

```java
lblPortada.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
```
| Línea | Qué hace |
|---|---|
| `setHorizontalAlignment(SwingConstants.CENTER)` | Centra la imagen de la portada dentro de su recuadro. Es solo una propiedad visual. |

---

## Cambio 5 — La portada de Pantalla3 se escala al tamaño del recuadro

Este cambio sí toca lógica y se hizo con autorización. Antes la imagen se mostraba en su tamaño
original: si el póster medía más de 440×660, solo se veía el centro recortado.

Antes:
```java
ImageIcon poster = new ImageIcon (contenido.getPortada());

Image posImage = poster.getImage();
lblPortada.setIcon(new ImageIcon(posImage));
lblPortada.setText("");
```
Después:
```java
ImageIcon poster = new ImageIcon (contenido.getPortada());

if (poster.getIconWidth() > 0) {
    int maxW = lblPortada.getWidth();
    int maxH = lblPortada.getHeight();
    double escala = Math.min((double) maxW / poster.getIconWidth(), (double) maxH / poster.getIconHeight());
    int w = (int) (poster.getIconWidth() * escala);
    int h = (int) (poster.getIconHeight() * escala);

    Image posImage = poster.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
    lblPortada.setIcon(new ImageIcon(posImage));
}
lblPortada.setText("");
```
| Línea | Qué hace |
|---|---|
| `ImageIcon poster = new ImageIcon(contenido.getPortada());` | Carga la imagen desde la ruta que se guardó en Pantalla2. No cambió. |
| `if (poster.getIconWidth() > 0) {` | Revisa que la imagen se haya cargado. Si no se eligió portada, la ruta está vacía y el ancho es `-1`. En ese caso se salta el escalado y el programa no falla. |
| `int maxW = lblPortada.getWidth();` | Ancho del recuadro (440 px). Ya se conoce aquí porque `initComponents()` termina con `pack()`, que acomoda los componentes y les da su tamaño. |
| `int maxH = lblPortada.getHeight();` | Alto del recuadro (660 px). |
| `double escala = Math.min(maxW / ancho, maxH / alto);` | Calcula cuánto hay que agrandar o achicar la imagen para que quepa. Se usa la menor de las dos escalas para que **no se deforme ni se recorte**. |
| `int w = (int) (poster.getIconWidth() * escala);` | Nuevo ancho de la imagen. |
| `int h = (int) (poster.getIconHeight() * escala);` | Nuevo alto de la imagen. Ancho y alto usan la misma escala, así que se conserva la proporción. |
| `Image posImage = poster.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);` | Crea la imagen redimensionada. `SCALE_SMOOTH` prioriza la calidad para que no se vea pixelada. |
| `lblPortada.setIcon(new ImageIcon(posImage));` | Pone la imagen ya escalada en el recuadro. |
| `lblPortada.setText("");` | Quita el texto del label. No cambió. |

Es el mismo cálculo que ya usaban en `elegirPortada()` de Pantalla2. Así las dos pantallas muestran
el póster de la misma forma.

---

## Cambio 6 — Limpieza

| Qué | Dónde | Por qué |
|---|---|---|
| Se borró el atributo `jScrollBar1` (`JScrollBar`) | `Pantalla2.java`: su creación en `initComponents()` y su declaración en las variables. `Pantalla2.form`: el bloque `<NonVisualComponents>` | Se creaba pero nunca se agregaba a la pantalla ni se usaba. Probablemente se arrastró por error desde la paleta de NetBeans. |
| Se quitó el comentario `// abrir maximizada` | `Pantalla2.java` y `Pantalla3.java`, después de `setExtendedState(...)` | Lo agregué en el cambio 1. Se quitó a pedido, para no dejar notas en el código. |

---

## Cambio 7 — La sinopsis de Pantalla2 crece hacia abajo

Antes, al escribir una sinopsis larga, el texto seguía en una sola línea hacia la derecha y aparecía
una barra de desplazamiento horizontal.

En el constructor de `Pantalla2.java`, junto a `TxtAR_Desc.setFont(Estilos.NORMAL);`:
```java
TxtAR_Desc.setLineWrap(true);
TxtAR_Desc.setWrapStyleWord(true);
```
| Línea | Qué hace |
|---|---|
| `TxtAR_Desc.setLineWrap(true);` | Cuando el texto llega al borde derecho, sigue en el renglón de abajo en lugar de extenderse hacia la derecha. Así ya no aparece la barra horizontal. |
| `TxtAR_Desc.setWrapStyleWord(true);` | Hace el salto de renglón entre palabras completas, sin cortar una palabra a la mitad. |

Cuando el texto llena el cuadro (186 px de alto), el `JScrollPane` que lo envuelve muestra
automáticamente una barra **vertical**. Pantalla3 ya usaba estas mismas dos líneas con `TA_Sinopsis`.

---

## Cambio 8 — Pantalla completa sin líneas blancas (Pantalla2 y Pantalla3)

**Problema reportado:** al pasar de Pantalla2 a Pantalla3, la ventana no quedaba en pantalla completa,
se veían restos de la pantalla anterior y el botón de maximizar (esquina superior derecha) aparecía activo.

**Primer intento (descartado):** antes de maximizar, se le dio a la ventana el tamaño de la pantalla con
`setBounds(...getMaximumWindowBounds())`. **Esto empeoró las cosas:** apareció una línea blanca de 1 px
en el borde derecho y en el inferior de **las dos** pantallas. Ese código ya se quitó.

**Cómo se encontró la causa:** se capturó el monitor con `java.awt.Robot` y se midió el brillo de la
última columna y la última fila de píxeles. Al probar distintas formas de abrir la ventana se vio que
la línea blanca aparece cuando, justo antes de maximizarse, la ventana está en la esquina (0,0) y es más
chica que la pantalla. Windows escala la pantalla al 125 %, y con ese escalado el último píxel queda sin
pintar. Pantalla2 no tenía el problema porque su panel ya medía 1920×1080 y la ventana estaba centrada.
Pantalla3 no cumplía ninguna de las dos cosas.

### Código final

`Pantalla2.java`. En el constructor, la línea original `setLocationRelativeTo(null);` se dejó como estaba
y después se llama al nuevo método:
```java
setLocationRelativeTo(null);
pantallaCompleta();
```
Y el método nuevo:
```java
private void pantallaCompleta() {
    setExtendedState(JFrame.MAXIMIZED_BOTH);
    addWindowListener(new java.awt.event.WindowAdapter() {
        @Override
        public void windowOpened(java.awt.event.WindowEvent e) {
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
    });
}
```

`Pantalla3.java`. Los dos constructores llaman a `pantallaCompleta();` justo después de `initComponents();`:
```java
private void pantallaCompleta() {
    setLocationRelativeTo(null);
    setExtendedState(MAXIMIZED_BOTH);
    addWindowListener(new java.awt.event.WindowAdapter() {
        @Override
        public void windowOpened(java.awt.event.WindowEvent e) {
            setExtendedState(MAXIMIZED_BOTH);
        }
    });
}
```
`Pantalla3.java` (`initComponents`) y `Pantalla3.form`, propiedad del panel de fondo:
```java
panelfondo.setPreferredSize(new java.awt.Dimension(1920, 1080));
```

| Línea / parte | Qué hace |
|---|---|
| `private void pantallaCompleta()` | Junta en un solo lugar todo lo necesario para abrir en pantalla completa. Pantalla3 lo usa desde sus dos constructores sin repetir código. |
| `setLocationRelativeTo(null);` | Centra la ventana antes de maximizarla, para que no parta en la esquina (0,0). Pantalla2 ya la tenía; en Pantalla3 se agregó. |
| `setExtendedState(MAXIMIZED_BOTH);` (primera) | Pide que la ventana se abra maximizada. Es lo mismo que había antes. |
| `addWindowListener(new java.awt.event.WindowAdapter() { ... })` | Registra un "oyente" que Java avisa cuando le pasa algo a la ventana. `WindowAdapter` permite escribir solo el evento que interesa. |
| `public void windowOpened(java.awt.event.WindowEvent e)` | Se ejecuta una sola vez, cuando la ventana ya apareció en pantalla. |
| `setExtendedState(MAXIMIZED_BOTH);` (dentro de `windowOpened`) | Vuelve a maximizar con la ventana ya visible. Si Windows ignoró la primera orden (el fallo original de Pantalla3), esta la corrige. |
| `panelfondo.setPreferredSize(new Dimension(1920, 1080));` | Hace que la ventana de Pantalla3, antes de maximizarse, mida lo mismo que la de Pantalla2. Con eso desaparece la línea blanca. Al maximizar, el contenido sigue centrado por los espacios flexibles del cambio 4. |

**Verificación:** se repitió 3 veces el recorrido Pantalla2 → Pantalla3 → Regresar → Pantalla2, midiendo los
bordes con capturas reales del monitor. En todos los pasos la ventana quedó maximizada (estado `6`) y los
bordes tienen el color del degradado (brillo 28–90), sin ninguna línea blanca (antes 226–227).

---

## Cambio 9 — El login pasa a ser Pantalla1

El login venía en otro proyecto (Maven, clase `Login.login`). Se pasó a este proyecto como `Pantalla1`,
que antes estaba vacía.

| Qué | Dónde | Por qué |
|---|---|---|
| Todo el código de `login.java` se copió a `Pantalla1.java` | `src/disneyplus/Pantalla1.java` | Así el login es la primera pantalla. Se cambió `package Login;` por `package disneyplus;` y el nombre `login` por `Pantalla1` (clase, constructor, `new Pantalla1()` y `Pantalla1.cerrarSesion(this)`). |
| Se borró `Pantalla1.form` | `src/disneyplus/` | Estaba vacío. El login está hecho a mano (componentes redondeados, `setLayout(null)`), no con el diseñador, así que el `.form` no le sirve y NetBeans marcaría error al abrirlo. |
| Se copió `uno.png` | `src/imagenes/uno.png` | Es el fondo del login. Se busca con `"/imagenes/uno.png"`, igual que en el proyecto original. |
| `DisneyPlus.main` abre `new Pantalla1()` | `src/disneyplus/DisneyPlus.java` | Antes abría `Pantalla2`. Ahora el programa empieza en el login. |

### `abrirCatalogo()` en Pantalla1

Antes (pendiente de conectar):
```java
liberarFormulario();
```
Después:
```java
new Pantalla2().setVisible(true);
dispose(); // cierra el login y detiene el reloj
```
| Línea | Qué hace |
|---|---|
| `new Pantalla2().setVisible(true);` | Con usuario y contraseña correctos, abre el gestor de películas y series. |
| `dispose();` | Cierra el login. `Pantalla1` sobrescribe `dispose()` para detener también el reloj. |
| ~~`liberarFormulario()`~~ | Se quitó junto con su método, que solo servía mientras Pantalla2 no estaba conectada. |

Recorrido: **Pantalla1 (login) → Pantalla2 (agregar) → Pantalla3 (detalle) → Regresar → Pantalla2.**

---

## Cambio 10 — Campos de Pantalla2 con el mismo estilo

`pintarFondoCampo(...)` dibuja el fondo gris redondeado con la línea inferior (azul con foco) y lo usan
`CampoTexto`, `ComboCampo` (combos de género y tipo) y `AreaCampo` (sinopsis). En el `.form` los tres
tienen código de creación personalizado. La etiqueta de `CampoTexto` ahora es un texto de ayuda que
aparece solo con el campo vacío (antes se encimaba con el texto en un campo de 32 px).

---

## Cambio 11 — Inventario estilo Disney+ con lista circular doblemente enlazada

Respaldo previo: `NetBeansProjects/DisneyPlus_checkpoint_2026-10-12`.

| Archivo | Qué cambió |
|---|---|
| `Nodo.java` (nuevo) | Nodo con `pelicula`, `siguiente` y `anterior`. |
| `ListaCircularDoble.java` (nueva) | `agregar`, `obtenerSiguiente`, `obtenerAnterior`, `obtenerActual`, `irA`, `buscar`, `eliminar`, `posicionActual`, `aLista`. Inserta al final, así que conserva el orden. Con lista vacía devuelve `null` sin fallar. |
| `Pantalla2.java` / `.form` | `Contenido` pasa a ser `static` y tiene `anio` y `cantidad`. `Contenido.CATALOGO` deja de ser un `ArrayList` y es la `ListaCircularDoble` (única colección de la app). Nueva fila "Año y cantidad" con validación, botón "Ver catálogo" y, si el título ya existe, se ofrece sumar unidades en lugar de duplicarlo. La sinopsis bajó de 186 a 136 px para que el panel conserve su alto. |
| `Pantalla3.java` | Reescrita a mano (como el login): barra superior, catálogo en tarjetas con buscador (ignora acentos), filtro por género, contadores, mensaje de catálogo vacío y ficha de detalles con "← Anterior" / "Siguiente →" sobre la lista circular, ajuste de existencias (− / +) y eliminar. Atajos en la ficha: flechas ← → y Esc. |
| `Pantalla3.form` | Eliminado: la pantalla ya no usa el diseñador. Queda una copia en el respaldo. |

Se quitaron el botón "REPRODUCIR" (no hacía nada) y "REGRESAR" (ahora es "+ Agregar película").
Las películas viven en memoria mientras el programa está abierto; al cerrarlo se pierden.

---

## Cambio 12 — Login sin líneas blancas (solo Pantalla1)

Se midieron los bordes con capturas reales del monitor (Windows al 125 %). Había dos líneas blancas:

| Línea | Causa | Arreglo en `Pantalla1.java` |
|---|---|---|
| Abajo (~7 px) | `uno.png` trae una franja blanca en sus últimas filas. | `FondoPanel.quitarFranjaBlanca(...)` recorta las filas finales casi blancas antes de dibujar la imagen. El archivo `uno.png` no se modificó. |
| Derecha (1 px, blanco puro) | La ventana se creaba a 1280×720 antes de maximizarse; con el escalado al 125 % Windows dejaba la última columna sin pintar (mismo problema del cambio 8). | `setSize(1920, 1080)` en lugar de 1280×720 y un `windowOpened` que vuelve a maximizar, igual que en Pantalla2 y Pantalla3. |

Además, el fondo del panel es azul oscuro en vez de gris claro, por si algún borde quedara sin cubrir.
Verificado 3 veces, también escribiendo en los campos: ninguna fila ni columna blanca en los bordes.

---

## Cambio 13 — Pantalla3 simplificada, con el estilo del login

Solo cambió el aspecto; el funcionamiento es el mismo.

| Antes | Ahora |
|---|---|
| Colores propios, brillo azul en el fondo | Colores de Pantalla1: campos `(10,40,70)`, borde `(60,120,180)`, botón `(0,114,210)`, esquinas de radio 20 |
| Barra superior con fondo y línea en degradado | Sin fondo, con una línea delgada como la del login |
| Contadores en "pastillas" de colores | Un renglón de texto: "6 títulos · 29 unidades disponibles · 1 agotado" |
| Tarjetas que se elevan con resplandor, etiquetas "PELICULA" y "NUEVO" | Al pasar el mouse solo aparece un borde azul; sin etiquetas sobre el póster |
| Existencias en etiqueta de color | Texto verde o rojo |
| Ficha con la portada desenfocada de fondo y etiquetas tipo/género/año | Fondo liso y un renglón "Pelicula · Romance · 2021" |
| Tres estilos de botón (incluido uno rojo) | Dos: el azul del login y uno con el estilo de sus campos |
| Buscador con lupa; portada de reemplazo con ícono y "SIN PORTADA" | Buscador como los campos del login; portada de reemplazo solo con el título |

Se borraron las clases y métodos que ya no se usaban (`Chip`, `PanelDetalle`, `crearFondo`, `pintarEtiqueta`).
