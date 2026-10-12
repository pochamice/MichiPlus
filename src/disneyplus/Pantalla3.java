package disneyplus;

import disneyplus.Pantalla2.Contenido;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicScrollBarUI;

/**
 * Inventario de películas: catálogo en tarjetas y ficha de detalles.
 *
 * Las dos vistas viven en la misma ventana (CardLayout). Las películas se leen de
 * Contenido.CATALOGO, la lista circular doblemente enlazada que comparte toda la aplicación,
 * y los botones "← Anterior" / "Siguiente →" de la ficha recorren sus nodos.
 */
public class Pantalla3 extends JFrame {

    // ===== Diseño =====
    public static class Estilos {

        // mismos colores que el login (Pantalla1)
        public static final Color FONDO = new Color(3, 14, 32);
        public static final Color FONDO_ALTO = new Color(8, 32, 58);
        public static final Color CAMPO = new Color(10, 40, 70);
        public static final Color BORDE = new Color(60, 120, 180);
        public static final Color BOTON = new Color(0, 114, 210);
        public static final Color BOTON_HOVER = new Color(0, 140, 240);
        public static final Color BOTON_PRESIONADO = new Color(0, 85, 160);
        public static final Color TEXTO = Color.WHITE;
        public static final Color TEXTO_SUAVE = new Color(170, 200, 230);
        public static final Color DISPONIBLE = new Color(110, 210, 160);
        public static final Color AGOTADO = new Color(240, 120, 120);

        public static final Font TITULO_BARRA = new Font("Segoe UI", Font.BOLD, 20);
        public static final Font TITULO_SECCION = new Font("Segoe UI", Font.BOLD, 26);
        public static final Font TITULO_DETALLE = new Font("Segoe UI", Font.BOLD, 36);
        public static final Font SUBTITULO = new Font("Segoe UI", Font.BOLD, 16);
        public static final Font NORMAL = new Font("Segoe UI", Font.PLAIN, 15);
        public static final Font PEQUENA = new Font("Segoe UI", Font.PLAIN, 13);
        public static final Font BOTON_TEXTO = new Font("Segoe UI", Font.BOLD, 14);
    }

    private static final int RADIO = 20;

    private static final String VISTA_CATALOGO = "catalogo";
    private static final String VISTA_DETALLE = "detalle";
    private static final String CENTRO_CUADRICULA = "cuadricula";
    private static final String CENTRO_VACIO = "vacio";
    private static final String TODOS_LOS_GENEROS = "Todos los géneros";

    // medidas de las tarjetas del catálogo
    private static final int MARGEN = 48;
    private static final int TARJETA_ANCHO = 200;
    private static final int POSTER_ALTO = 300;
    private static final int TARJETA_ALTO = POSTER_ALTO + 72;
    private static final int ESPACIO_H = 28;
    private static final int ESPACIO_V = 26;
    private static final int RADIO_POSTER = 14;

    // medidas de la ficha de detalles
    private static final int DETALLE_POSTER_ANCHO = 340;
    private static final int DETALLE_POSTER_ALTO = 510;
    private static final int DETALLE_INFO_ANCHO = 640;

    // Portadas ya escaladas, para no volver a leer el disco cada vez que se redibuja.
    private static final Map<String, BufferedImage> CACHE_IMAGENES = new HashMap<>();

    // ===== Datos =====
    private final ListaCircularDoble lista = Contenido.CATALOGO;
    private final Contenido resaltado;
    private boolean enDetalle = false;
    private boolean actualizandoGeneros = false;

    // ===== Componentes =====
    private final CardLayout vistas = new CardLayout();
    private final JPanel panelVistas = new JPanel(vistas);

    private CampoBusqueda txtBuscar;
    private JComboBox<String> cmbGenero;
    private JLabel lblResumen;
    private JLabel lblResultado;
    private final CardLayout centro = new CardLayout();
    private final JPanel panelCentro = new JPanel(centro);
    private final JPanel cuadricula = new JPanel();
    private JScrollPane scrollCatalogo;
    private int columnas = 0;
    private TarjetaPelicula tarjetaResaltada;
    private PanelVacio panelVacio;

    private JPanel panelDetalle;
    private JLabel lblPosterDetalle;
    private JLabel lblTituloDetalle;
    private JLabel lblDatosDetalle;
    private JLabel lblCantidadDetalle;
    private JLabel lblEstadoDetalle;
    private JTextArea txtSinopsisDetalle;
    private JScrollPane scrollSinopsis;
    private JLabel lblPosicion;
    private Boton btnAnterior;
    private Boton btnSiguiente;
    private Boton btnMenos;
    private Boton btnMas;

    public Pantalla3() {
        this(null);
    }

    // "resaltado" es la película recién agregada desde Pantalla2 (puede ser null).
    public Pantalla3(Contenido resaltado) {
        this.resaltado = resaltado;

        setTitle("Disney+ - Inventario de películas");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        FondoPantalla raiz = new FondoPantalla();
        raiz.setLayout(new BorderLayout());
        raiz.setPreferredSize(new Dimension(1920, 1080));
        setContentPane(raiz);

        panelVistas.setOpaque(false);
        panelVistas.add(construirCatalogo(), VISTA_CATALOGO);
        panelVistas.add(construirDetalle(), VISTA_DETALLE);

        raiz.add(construirBarraSuperior(), BorderLayout.NORTH);
        raiz.add(panelVistas, BorderLayout.CENTER);

        registrarAtajos();
        actualizarGeneros();
        aplicarFiltros();

        pack();
        pantallaCompleta();
    }

    private void pantallaCompleta() {
        setLocationRelativeTo(null);
        setExtendedState(MAXIMIZED_BOTH);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                setExtendedState(MAXIMIZED_BOTH);
                // cuando la ventana ya tiene su tamaño final, se lleva la vista a la película nueva
                SwingUtilities.invokeLater(() -> {
                    if (tarjetaResaltada != null) {
                        tarjetaResaltada.scrollRectToVisible(new Rectangle(0, 0,
                                tarjetaResaltada.getWidth(), tarjetaResaltada.getHeight()));
                    }
                });
            }
        });
    }

    // ===== Barra superior =====
    private JPanel construirBarraSuperior() {
        JPanel barra = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                // línea delgada debajo de la barra, como la del login
                g.setColor(Estilos.BORDE);
                g.fillRect(MARGEN, getHeight() - 2, getWidth() - 2 * MARGEN, 2);
            }
        };
        barra.setOpaque(false);
        barra.setBorder(BorderFactory.createEmptyBorder(14, MARGEN, 16, MARGEN));

        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        izquierda.setOpaque(false);
        Image logo = cargarLogo(46);
        if (logo != null) {
            izquierda.add(new JLabel(new ImageIcon(logo)));
        }
        JLabel lblSistema = new JLabel("Inventario de películas");
        lblSistema.setFont(Estilos.TITULO_BARRA);
        lblSistema.setForeground(Estilos.TEXTO);
        izquierda.add(lblSistema);

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        derecha.setOpaque(false);
        String nombre = Pantalla1.getNombreActual();
        if (nombre != null) {
            JLabel lblUsuario = new JLabel("Hola, " + nombre);
            lblUsuario.setFont(Estilos.NORMAL);
            lblUsuario.setForeground(Estilos.TEXTO_SUAVE);
            lblUsuario.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
            derecha.add(lblUsuario);
        }
        Boton btnAgregar = new Boton("+ Agregar película", Boton.PRIMARIO);
        btnAgregar.addActionListener(e -> irAlFormulario());
        Boton btnCerrarSesion = new Boton("Cerrar sesión", Boton.SECUNDARIO);
        btnCerrarSesion.addActionListener(e -> Pantalla1.cerrarSesion(this));
        derecha.add(btnAgregar);
        derecha.add(btnCerrarSesion);

        barra.add(izquierda, BorderLayout.WEST);
        barra.add(derecha, BorderLayout.EAST);
        return barra;
    }

    // ===== Vista: catálogo =====
    private JPanel construirCatalogo() {
        JPanel catalogo = new JPanel(new BorderLayout());
        catalogo.setOpaque(false);

        // fila de título, contadores y filtros
        JPanel herramientas = new JPanel(new BorderLayout(24, 0));
        herramientas.setOpaque(false);
        herramientas.setBorder(BorderFactory.createEmptyBorder(28, MARGEN, 20, MARGEN));

        // título y contadores en dos renglones de texto
        JPanel resumen = new JPanel();
        resumen.setOpaque(false);
        resumen.setLayout(new BoxLayout(resumen, BoxLayout.Y_AXIS));
        JLabel lblCatalogo = new JLabel("Catálogo");
        lblCatalogo.setFont(Estilos.TITULO_SECCION);
        lblCatalogo.setForeground(Estilos.TEXTO);
        lblResumen = new JLabel();
        lblResumen.setFont(Estilos.NORMAL);
        lblResumen.setForeground(Estilos.TEXTO_SUAVE);
        resumen.add(lblCatalogo);
        resumen.add(lblResumen);

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        filtros.setOpaque(false);
        lblResultado = new JLabel();
        lblResultado.setFont(Estilos.PEQUENA);
        lblResultado.setForeground(Estilos.TEXTO_SUAVE);
        txtBuscar = new CampoBusqueda("Buscar por título...");
        txtBuscar.setPreferredSize(new Dimension(320, 42));
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                aplicarFiltros();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                aplicarFiltros();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                aplicarFiltros();
            }
        });
        cmbGenero = new ComboOscuro<>();
        cmbGenero.setPreferredSize(new Dimension(220, 42));
        cmbGenero.addActionListener(e -> {
            if (!actualizandoGeneros) {
                aplicarFiltros();
            }
        });
        filtros.add(lblResultado);
        filtros.add(txtBuscar);
        filtros.add(cmbGenero);

        // los filtros quedan centrados en vertical respecto al título
        JPanel envolturaFiltros = new JPanel(new GridBagLayout());
        envolturaFiltros.setOpaque(false);
        envolturaFiltros.add(filtros);

        herramientas.add(resumen, BorderLayout.WEST);
        herramientas.add(envolturaFiltros, BorderLayout.EAST);

        // cuadrícula de tarjetas dentro de un panel con desplazamiento vertical
        cuadricula.setOpaque(false);
        cuadricula.setLayout(new GridLayout(0, 1, ESPACIO_H, ESPACIO_V));
        PanelDesplazable contenedor = new PanelDesplazable(new FlowLayout(FlowLayout.LEFT, 0, 0));
        contenedor.setBorder(BorderFactory.createEmptyBorder(6, MARGEN, 48, MARGEN));
        contenedor.add(cuadricula);

        scrollCatalogo = crearScroll(contenedor);
        scrollCatalogo.getViewport().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                recalcularColumnas();
            }
        });

        panelVacio = new PanelVacio();

        panelCentro.setOpaque(false);
        panelCentro.add(scrollCatalogo, CENTRO_CUADRICULA);
        panelCentro.add(panelVacio, CENTRO_VACIO);

        catalogo.add(herramientas, BorderLayout.NORTH);
        catalogo.add(panelCentro, BorderLayout.CENTER);
        return catalogo;
    }

    // Cuántas tarjetas caben por fila según el ancho disponible.
    private void recalcularColumnas() {
        int ancho = scrollCatalogo.getViewport().getWidth() - 2 * MARGEN;
        int nuevas = Math.max(1, (ancho + ESPACIO_H) / (TARJETA_ANCHO + ESPACIO_H));
        if (nuevas != columnas) {
            columnas = nuevas;
            cuadricula.setLayout(new GridLayout(0, columnas, ESPACIO_H, ESPACIO_V));
            cuadricula.revalidate();
            cuadricula.repaint();
        }
    }

    // Llena el filtro con los géneros que existen en el inventario, en el orden en que aparecen.
    private void actualizarGeneros() {
        Object seleccionado = cmbGenero.getSelectedItem();
        LinkedHashSet<String> generos = new LinkedHashSet<>();
        for (Contenido c : lista.aLista()) {
            generos.add(c.getGenero());
        }

        actualizandoGeneros = true;
        cmbGenero.removeAllItems();
        cmbGenero.addItem(TODOS_LOS_GENEROS);
        for (String genero : generos) {
            cmbGenero.addItem(genero);
        }
        if (seleccionado != null && generos.contains(seleccionado.toString())) {
            cmbGenero.setSelectedItem(seleccionado);
        } else {
            cmbGenero.setSelectedIndex(0);
        }
        actualizandoGeneros = false;
    }

    // Vuelve a dibujar las tarjetas según el texto buscado y el género elegido.
    private void aplicarFiltros() {
        String buscado = normalizar(txtBuscar.getText());
        String genero = cmbGenero.getSelectedIndex() > 0 ? (String) cmbGenero.getSelectedItem() : null;

        ArrayList<Contenido> todas = lista.aLista();
        ArrayList<Contenido> visibles = new ArrayList<>();
        for (Contenido c : todas) {
            boolean coincideTitulo = buscado.isEmpty() || normalizar(c.getNombre()).contains(buscado);
            boolean coincideGenero = genero == null || genero.equals(c.getGenero());
            if (coincideTitulo && coincideGenero) {
                visibles.add(c);
            }
        }

        cuadricula.removeAll();
        tarjetaResaltada = null;
        for (Contenido c : visibles) {
            TarjetaPelicula tarjeta = new TarjetaPelicula(c);
            if (c == resaltado) {
                tarjetaResaltada = tarjeta;
            }
            cuadricula.add(tarjeta);
        }
        cuadricula.revalidate();
        cuadricula.repaint();

        actualizarContadores(todas, visibles.size());

        if (todas.isEmpty()) {
            panelVacio.mostrar("Aún no hay películas registradas",
                    "Agrega la primera película para comenzar tu inventario.",
                    "+ Agregar película", this::irAlFormulario);
            centro.show(panelCentro, CENTRO_VACIO);
        } else if (visibles.isEmpty()) {
            panelVacio.mostrar("No encontramos coincidencias",
                    "Prueba con otro título o elige un género diferente.",
                    "Limpiar filtros", this::limpiarFiltros);
            centro.show(panelCentro, CENTRO_VACIO);
        } else {
            centro.show(panelCentro, CENTRO_CUADRICULA);
        }
    }

    private void actualizarContadores(ArrayList<Contenido> todas, int visibles) {
        int unidades = 0;
        int agotados = 0;
        for (Contenido c : todas) {
            unidades += c.getCantidad();
            if (c.getCantidad() == 0) {
                agotados++;
            }
        }
        String resumen = todas.size() + (todas.size() == 1 ? " título" : " títulos")
                + "   ·   " + unidades + (unidades == 1 ? " unidad disponible" : " unidades disponibles");
        if (agotados > 0) {
            resumen += "   ·   " + agotados + (agotados == 1 ? " agotado" : " agotados");
        }
        lblResumen.setText(resumen);
        lblResultado.setText(visibles < todas.size() ? "Mostrando " + visibles + " de " + todas.size() : "");
    }

    private void limpiarFiltros() {
        actualizandoGeneros = true;
        cmbGenero.setSelectedIndex(0);
        actualizandoGeneros = false;
        txtBuscar.setText("");
        aplicarFiltros();
    }

    // ===== Vista: ficha de detalles =====
    private JPanel construirDetalle() {
        panelDetalle = new JPanel(new BorderLayout());
        panelDetalle.setOpaque(false);

        JPanel superior = new JPanel(new BorderLayout());
        superior.setOpaque(false);
        superior.setBorder(BorderFactory.createEmptyBorder(24, MARGEN, 0, MARGEN));
        Boton btnVolver = new Boton("← Volver al catálogo", Boton.SECUNDARIO);
        btnVolver.addActionListener(e -> volverAlCatalogo());
        JPanel envolturaVolver = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        envolturaVolver.setOpaque(false);
        envolturaVolver.add(btnVolver);
        lblPosicion = new JLabel();
        lblPosicion.setFont(Estilos.NORMAL);
        lblPosicion.setForeground(Estilos.TEXTO_SUAVE);
        superior.add(envolturaVolver, BorderLayout.WEST);
        superior.add(lblPosicion, BorderLayout.EAST);

        // póster a la izquierda, información a la derecha
        lblPosterDetalle = new JLabel();
        lblPosterDetalle.setPreferredSize(new Dimension(DETALLE_POSTER_ANCHO, DETALLE_POSTER_ALTO));
        lblPosterDetalle.setVerticalAlignment(SwingConstants.TOP);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        lblDatosDetalle = new JLabel();
        lblDatosDetalle.setFont(Estilos.SUBTITULO);
        lblDatosDetalle.setForeground(Estilos.TEXTO_SUAVE);
        lblDatosDetalle.setAlignmentX(LEFT_ALIGNMENT);

        lblTituloDetalle = new JLabel();
        lblTituloDetalle.setFont(Estilos.TITULO_DETALLE);
        lblTituloDetalle.setForeground(Estilos.TEXTO);
        lblTituloDetalle.setAlignmentX(LEFT_ALIGNMENT);

        // caja de existencias
        CajaRedondeada inventario = new CajaRedondeada(new BorderLayout(16, 0));
        inventario.setBorder(BorderFactory.createEmptyBorder(16, 22, 16, 22));
        JPanel textosInventario = new JPanel();
        textosInventario.setOpaque(false);
        textosInventario.setLayout(new BoxLayout(textosInventario, BoxLayout.Y_AXIS));
        JLabel lblTituloInventario = new JLabel("Unidades disponibles");
        lblTituloInventario.setFont(Estilos.PEQUENA);
        lblTituloInventario.setForeground(Estilos.TEXTO_SUAVE);
        lblCantidadDetalle = new JLabel();
        lblCantidadDetalle.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblCantidadDetalle.setForeground(Estilos.TEXTO);
        lblEstadoDetalle = new JLabel();
        lblEstadoDetalle.setFont(Estilos.PEQUENA);
        textosInventario.add(lblTituloInventario);
        textosInventario.add(lblCantidadDetalle);
        textosInventario.add(lblEstadoDetalle);

        JPanel ajustes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        ajustes.setOpaque(false);
        btnMenos = new Boton("−", Boton.SECUNDARIO);
        btnMas = new Boton("+", Boton.SECUNDARIO);
        for (Boton b : new Boton[]{btnMenos, btnMas}) {
            b.setFont(new Font("Segoe UI", Font.BOLD, 22));
            b.setBorder(BorderFactory.createEmptyBorder());
            b.setPreferredSize(new Dimension(48, 48));
        }
        btnMenos.setToolTipText("Quitar una unidad");
        btnMas.setToolTipText("Agregar una unidad");
        btnMenos.addActionListener(e -> cambiarCantidad(-1));
        btnMas.addActionListener(e -> cambiarCantidad(1));
        ajustes.add(btnMenos);
        ajustes.add(btnMas);
        JPanel envolturaAjustes = new JPanel(new GridBagLayout());
        envolturaAjustes.setOpaque(false);
        envolturaAjustes.add(ajustes);

        inventario.add(textosInventario, BorderLayout.CENTER);
        inventario.add(envolturaAjustes, BorderLayout.EAST);
        inventario.setAlignmentX(LEFT_ALIGNMENT);
        inventario.setMaximumSize(new Dimension(DETALLE_INFO_ANCHO, 120));

        JLabel lblSinopsis = new JLabel("Sinopsis");
        lblSinopsis.setFont(Estilos.SUBTITULO);
        lblSinopsis.setForeground(Estilos.TEXTO_SUAVE);
        lblSinopsis.setAlignmentX(LEFT_ALIGNMENT);

        txtSinopsisDetalle = new JTextArea();
        txtSinopsisDetalle.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        txtSinopsisDetalle.setForeground(new Color(220, 228, 240));
        txtSinopsisDetalle.setLineWrap(true);
        txtSinopsisDetalle.setWrapStyleWord(true);
        txtSinopsisDetalle.setEditable(false);
        txtSinopsisDetalle.setFocusable(false);
        txtSinopsisDetalle.setOpaque(false);
        txtSinopsisDetalle.setBorder(null);
        scrollSinopsis = crearScroll(txtSinopsisDetalle);
        scrollSinopsis.setAlignmentX(LEFT_ALIGNMENT);
        scrollSinopsis.setPreferredSize(new Dimension(DETALLE_INFO_ANCHO, 150));
        scrollSinopsis.setMaximumSize(new Dimension(DETALLE_INFO_ANCHO, 150));

        // navegación por la lista circular doblemente enlazada
        btnAnterior = new Boton("← Anterior", Boton.SECUNDARIO);
        btnSiguiente = new Boton("Siguiente →", Boton.PRIMARIO);
        Boton btnEliminar = new Boton("Eliminar", Boton.SECUNDARIO);
        btnAnterior.setPreferredSize(new Dimension(160, 46));
        btnSiguiente.setPreferredSize(new Dimension(160, 46));
        btnEliminar.setPreferredSize(new Dimension(140, 46));
        btnAnterior.addActionListener(e -> mostrarAnterior());
        btnSiguiente.addActionListener(e -> mostrarSiguiente());
        btnEliminar.addActionListener(e -> eliminarActual());
        JPanel filaNavegacion = filaIzquierda(12, btnAnterior, btnSiguiente, btnEliminar);

        info.add(lblDatosDetalle);
        info.add(Box.createVerticalStrut(6));
        info.add(lblTituloDetalle);
        info.add(Box.createVerticalStrut(22));
        info.add(inventario);
        info.add(Box.createVerticalStrut(24));
        info.add(lblSinopsis);
        info.add(Box.createVerticalStrut(8));
        info.add(scrollSinopsis);
        info.add(Box.createVerticalStrut(28));
        info.add(filaNavegacion);

        JPanel contenido = new JPanel(new BorderLayout(56, 0));
        contenido.setOpaque(false);
        contenido.add(lblPosterDetalle, BorderLayout.WEST);
        contenido.add(info, BorderLayout.CENTER);

        JPanel centrado = new JPanel(new GridBagLayout());
        centrado.setOpaque(false);
        centrado.add(contenido);

        panelDetalle.add(superior, BorderLayout.NORTH);
        panelDetalle.add(centrado, BorderLayout.CENTER);
        return panelDetalle;
    }

    // Fila alineada a la izquierda con un espacio fijo entre sus componentes.
    private static JPanel filaIzquierda(int espacio, JComponent... hijos) {
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        fila.setOpaque(false);
        fila.setAlignmentX(LEFT_ALIGNMENT);
        for (int i = 0; i < hijos.length; i++) {
            if (i > 0) {
                fila.add(Box.createHorizontalStrut(espacio));
            }
            fila.add(hijos[i]);
        }
        return fila;
    }

    // Abre la ficha de una película y deja ese nodo como "actual" en la lista.
    private void mostrarDetalle(Contenido pelicula) {
        if (!lista.irA(pelicula)) {
            return;
        }
        pintarDetalle(lista.obtenerActual());
        enDetalle = true;
        vistas.show(panelVistas, VISTA_DETALLE);
    }

    private void mostrarSiguiente() {
        Contenido pelicula = lista.obtenerSiguiente();
        if (pelicula != null) {
            pintarDetalle(pelicula);
        }
    }

    private void mostrarAnterior() {
        Contenido pelicula = lista.obtenerAnterior();
        if (pelicula != null) {
            pintarDetalle(pelicula);
        }
    }

    private void pintarDetalle(Contenido c) {
        if (c == null) {
            return;
        }
        lblDatosDetalle.setText(c.getTipo() + "   ·   " + c.getGenero() + "   ·   " + c.getAnio());
        lblTituloDetalle.setText("<html><body style='width: " + (DETALLE_INFO_ANCHO - 20) + "px'>"
                + escaparHtml(c.getNombre()) + "</body></html>");

        lblPosterDetalle.setIcon(new ImageIcon(crearPortada(c, DETALLE_POSTER_ANCHO, DETALLE_POSTER_ALTO, RADIO, 30f)));

        txtSinopsisDetalle.setText(c.getSinopsis());
        txtSinopsisDetalle.setCaretPosition(0);

        actualizarInventarioDetalle(c);

        int total = lista.tamanio();
        lblPosicion.setText("Título " + lista.posicionActual() + " de " + total);
        boolean variasPeliculas = total > 1;
        btnAnterior.setEnabled(variasPeliculas);
        btnSiguiente.setEnabled(variasPeliculas);
        String ayuda = variasPeliculas ? null : "Solo hay un título en el inventario";
        btnAnterior.setToolTipText(ayuda);
        btnSiguiente.setToolTipText(ayuda);

        panelDetalle.revalidate();
        panelDetalle.repaint();
    }

    private void actualizarInventarioDetalle(Contenido c) {
        int cantidad = c.getCantidad();
        lblCantidadDetalle.setText(String.valueOf(cantidad));
        if (cantidad > 0) {
            lblEstadoDetalle.setText("En existencia");
            lblEstadoDetalle.setForeground(Estilos.DISPONIBLE);
        } else {
            lblEstadoDetalle.setText("Agotado");
            lblEstadoDetalle.setForeground(Estilos.AGOTADO);
        }
        btnMenos.setEnabled(cantidad > 0);
    }

    private void cambiarCantidad(int cambio) {
        Contenido c = lista.obtenerActual();
        if (c != null) {
            c.setCantidad(c.getCantidad() + cambio);
            actualizarInventarioDetalle(c);
        }
    }

    private void eliminarActual() {
        Contenido c = lista.obtenerActual();
        if (c == null) {
            return;
        }
        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Eliminar \"" + c.getNombre() + "\" del inventario?\nEsta acción no se puede deshacer.",
                "Eliminar título", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }
        lista.eliminar(c); // la lista mueve "actual" al nodo siguiente
        if (lista.estaVacia()) {
            volverAlCatalogo();
        } else {
            pintarDetalle(lista.obtenerActual());
        }
    }

    private void volverAlCatalogo() {
        enDetalle = false;
        actualizarGeneros();
        aplicarFiltros(); // refleja cambios de existencias o eliminaciones
        vistas.show(panelVistas, VISTA_CATALOGO);
    }

    private void irAlFormulario() {
        new Pantalla2().setVisible(true);
        dispose();
    }

    // Flechas izquierda/derecha recorren la lista y Escape regresa al catálogo (solo en la ficha).
    private void registrarAtajos() {
        InputMap teclas = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap acciones = getRootPane().getActionMap();
        teclas.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "anterior");
        teclas.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "siguiente");
        teclas.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "volver");
        acciones.put("anterior", accion(() -> {
            if (enDetalle && btnAnterior.isEnabled()) {
                mostrarAnterior();
            }
        }));
        acciones.put("siguiente", accion(() -> {
            if (enDetalle && btnSiguiente.isEnabled()) {
                mostrarSiguiente();
            }
        }));
        acciones.put("volver", accion(() -> {
            if (enDetalle) {
                volverAlCatalogo();
            }
        }));
    }

    private static Action accion(Runnable tarea) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tarea.run();
            }
        };
    }

    // ===== Utilidades de texto =====
    private static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        // quita acentos para que "accion" encuentre "Acción"
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase().trim();
    }

    private static String escaparHtml(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String recortar(String texto, FontMetrics fm, int ancho) {
        if (fm.stringWidth(texto) <= ancho) {
            return texto;
        }
        String puntos = "...";
        int fin = texto.length();
        while (fin > 0 && fm.stringWidth(texto.substring(0, fin) + puntos) > ancho) {
            fin--;
        }
        return texto.substring(0, fin).trim() + puntos;
    }

    // Parte el texto en renglones que quepan en el ancho dado.
    private static ArrayList<String> envolver(String texto, FontMetrics fm, int ancho, int maxRenglones) {
        ArrayList<String> renglones = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        for (String palabra : texto.trim().split("\\s+")) {
            String prueba = actual.length() == 0 ? palabra : actual + " " + palabra;
            if (fm.stringWidth(prueba) <= ancho || actual.length() == 0) {
                actual.setLength(0);
                actual.append(prueba);
            } else {
                renglones.add(actual.toString());
                actual.setLength(0);
                actual.append(palabra);
            }
        }
        if (actual.length() > 0) {
            renglones.add(actual.toString());
        }
        if (renglones.size() > maxRenglones) {
            ArrayList<String> cortados = new ArrayList<>(renglones.subList(0, maxRenglones));
            String ultimo = cortados.get(maxRenglones - 1) + " " + renglones.get(maxRenglones);
            cortados.set(maxRenglones - 1, recortar(ultimo + "...", fm, ancho));
            return cortados;
        }
        for (int i = 0; i < renglones.size(); i++) {
            renglones.set(i, recortar(renglones.get(i), fm, ancho));
        }
        return renglones;
    }

    // ===== Imágenes =====
    private static BufferedImage cargarImagen(String ruta) {
        if (ruta == null || ruta.isBlank()) {
            return null;
        }
        File archivo = new File(ruta);
        if (!archivo.isFile()) {
            return null;
        }
        try {
            return ImageIO.read(archivo);
        } catch (IOException e) {
            return null;
        }
    }

    // Escala la imagen para cubrir todo el recuadro, recortando lo que sobre (sin deformarla).
    private static BufferedImage cubrir(Image original, int ancho, int alto) {
        int iw = original.getWidth(null);
        int ih = original.getHeight(null);
        double escala = Math.max((double) ancho / iw, (double) alto / ih);
        int w = Math.max(1, (int) Math.ceil(iw * escala));
        int h = Math.max(1, (int) Math.ceil(ih * escala));
        Image escalada = new ImageIcon(original.getScaledInstance(w, h, Image.SCALE_SMOOTH)).getImage();

        BufferedImage resultado = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resultado.createGraphics();
        g2.drawImage(escalada, (ancho - w) / 2, (alto - h) / 2, null);
        g2.dispose();
        return resultado;
    }

    // Póster con esquinas redondeadas. Si no hay imagen, dibuja un póster de reemplazo con el título.
    private static BufferedImage crearPortada(Contenido c, int ancho, int alto, int radio, float tamTitulo) {
        String clave = c.getPortada() + "#" + ancho + "x" + alto + "#" + radio;
        BufferedImage guardada = CACHE_IMAGENES.get(clave);
        if (guardada != null) {
            return guardada;
        }

        BufferedImage original = cargarImagen(c.getPortada());
        BufferedImage portada = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = portada.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // primero la forma redondeada; lo demás se pinta solo dentro de ella (SrcAtop)
        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Float(0, 0, ancho, alto, radio, radio));
        g2.setComposite(AlphaComposite.SrcAtop);

        if (original != null) {
            g2.drawImage(cubrir(original, ancho, alto), 0, 0, null);
        } else {
            pintarPortadaReemplazo(g2, c, ancho, alto, tamTitulo);
        }

        // contorno fino del mismo azul que los campos del login
        g2.setComposite(AlphaComposite.SrcOver);
        g2.setColor(Estilos.BORDE);
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, ancho - 1, alto - 1, radio, radio));
        g2.dispose();

        // solo se guardan las portadas reales: las de reemplazo dependen del título
        if (original != null) {
            CACHE_IMAGENES.put(clave, portada);
        }
        return portada;
    }

    private static void pintarPortadaReemplazo(Graphics2D g2, Contenido c, int ancho, int alto, float tamTitulo) {
        g2.setPaint(new GradientPaint(0, 0, Estilos.CAMPO, 0, alto, Estilos.FONDO));
        g2.fillRect(0, 0, ancho, alto);

        // título centrado
        Font fuente = Estilos.SUBTITULO.deriveFont(tamTitulo);
        g2.setFont(fuente);
        FontMetrics fm = g2.getFontMetrics();
        ArrayList<String> renglones = envolver(c.getNombre(), fm, ancho - 32, 4);
        int y = (alto - renglones.size() * fm.getHeight()) / 2 + fm.getAscent();
        g2.setColor(Color.WHITE);
        for (String renglon : renglones) {
            g2.drawString(renglon, (ancho - fm.stringWidth(renglon)) / 2, y);
            y += fm.getHeight();
        }

    }

    // El logo es blanco sobre fondo transparente: se recorta al área visible y se escala.
    private static Image cargarLogo(int alto) {
        URL url = Pantalla3.class.getResource("/imagenes/michi_plus_1.png");
        if (url == null) {
            return null;
        }
        try {
            BufferedImage img = ImageIO.read(url);
            int x0 = img.getWidth(), y0 = img.getHeight(), x1 = -1, y1 = -1;
            for (int y = 0; y < img.getHeight(); y++) {
                for (int x = 0; x < img.getWidth(); x++) {
                    if ((img.getRGB(x, y) >>> 24) > 16) {
                        x0 = Math.min(x0, x);
                        y0 = Math.min(y0, y);
                        x1 = Math.max(x1, x);
                        y1 = Math.max(y1, y);
                    }
                }
            }
            if (x1 < 0) {
                return null;
            }
            BufferedImage recorte = img.getSubimage(x0, y0, x1 - x0 + 1, y1 - y0 + 1);
            int ancho = recorte.getWidth() * alto / recorte.getHeight();
            return new ImageIcon(recorte.getScaledInstance(ancho, alto, Image.SCALE_SMOOTH)).getImage();
        } catch (IOException e) {
            return null;
        }
    }

    private static JScrollPane crearScroll(Component vista) {
        JScrollPane scroll = new JScrollPane(vista);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setViewportBorder(null);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUI(new BarraDesplazamiento());
        scroll.getVerticalScrollBar().setOpaque(false);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        scroll.getVerticalScrollBar().setUnitIncrement(24);
        return scroll;
    }

    // ===== Componentes visuales =====

    // Fondo de toda la ventana: degradado azul marino, como el del login.
    private static class FondoPantalla extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            int w = getWidth();
            int h = getHeight();
            g2.setPaint(new GradientPaint(0, 0, Estilos.FONDO_ALTO, 0, h, Estilos.FONDO));
            g2.fillRect(0, 0, w, h);
            g2.dispose();
        }
    }

    // Tarjeta del catálogo: póster, título, género, año y existencias. Se marca al pasar el mouse.
    private class TarjetaPelicula extends JComponent {

        private final Contenido pelicula;
        private final BufferedImage portada;
        private boolean encima = false;

        TarjetaPelicula(Contenido pelicula) {
            this.pelicula = pelicula;
            this.portada = crearPortada(pelicula, TARJETA_ANCHO, POSTER_ALTO, RADIO_POSTER, 20f);
            setPreferredSize(new Dimension(TARJETA_ANCHO, TARJETA_ALTO));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText(pelicula.getNombre());

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    encima = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    encima = false;
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        mostrarDetalle(pelicula);
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            g2.drawImage(portada, 0, 0, null);
            if (encima) {
                g2.setColor(Estilos.BOTON_HOVER);
                g2.setStroke(new BasicStroke(3f));
                g2.draw(new RoundRectangle2D.Float(1.5f, 1.5f, TARJETA_ANCHO - 3, POSTER_ALTO - 3,
                        RADIO_POSTER, RADIO_POSTER));
            }

            // textos debajo del póster
            int ty = POSTER_ALTO + 24;
            g2.setFont(Estilos.SUBTITULO);
            g2.setColor(Estilos.TEXTO);
            g2.drawString(recortar(pelicula.getNombre(), g2.getFontMetrics(), TARJETA_ANCHO - 4), 2, ty);

            ty += 20;
            g2.setFont(Estilos.PEQUENA);
            g2.setColor(Estilos.TEXTO_SUAVE);
            g2.drawString(recortar(pelicula.getGenero() + "  ·  " + pelicula.getAnio(),
                    g2.getFontMetrics(), TARJETA_ANCHO - 4), 2, ty);

            ty += 19;
            int cantidad = pelicula.getCantidad();
            g2.setColor(cantidad == 0 ? Estilos.AGOTADO : Estilos.DISPONIBLE);
            g2.drawString(cantidad == 0 ? "Agotado"
                    : cantidad + (cantidad == 1 ? " disponible" : " disponibles"), 2, ty);
            g2.dispose();
        }
    }

    // Mensaje que se muestra cuando no hay películas o la búsqueda no encuentra nada.
    private static class PanelVacio extends JPanel {

        private final JLabel lblTitulo = new JLabel();
        private final JLabel lblMensaje = new JLabel();
        private final Boton btnAccion = new Boton("", Boton.PRIMARIO);
        private Runnable accion;

        PanelVacio() {
            setOpaque(false);
            setLayout(new GridBagLayout());

            JPanel bloque = new JPanel();
            bloque.setOpaque(false);
            bloque.setLayout(new BoxLayout(bloque, BoxLayout.Y_AXIS));

            lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
            lblTitulo.setForeground(Estilos.TEXTO);
            lblMensaje.setFont(Estilos.NORMAL);
            lblMensaje.setForeground(Estilos.TEXTO_SUAVE);
            btnAccion.setPreferredSize(new Dimension(220, 46));
            btnAccion.setMaximumSize(new Dimension(220, 46));
            btnAccion.addActionListener(e -> {
                if (accion != null) {
                    accion.run();
                }
            });

            for (JComponent c : new JComponent[]{lblTitulo, lblMensaje, btnAccion}) {
                c.setAlignmentX(CENTER_ALIGNMENT);
            }
            bloque.add(lblTitulo);
            bloque.add(Box.createVerticalStrut(8));
            bloque.add(lblMensaje);
            bloque.add(Box.createVerticalStrut(24));
            bloque.add(btnAccion);
            add(bloque);
        }

        void mostrar(String titulo, String mensaje, String textoBoton, Runnable accion) {
            lblTitulo.setText(titulo);
            lblMensaje.setText(mensaje);
            btnAccion.setText(textoBoton);
            this.accion = accion;
        }
    }

    // Botón redondeado como el de Pantalla1: principal (azul) o secundario (color de campo con borde).
    private static class Boton extends JButton {

        static final int PRIMARIO = 0;
        static final int SECUNDARIO = 1;

        private final int estilo;
        private boolean encima = false;

        Boton(String texto, int estilo) {
            super(texto);
            this.estilo = estilo;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setFont(Estilos.BOTON_TEXTO);
            setForeground(Color.WHITE);
            setBorder(BorderFactory.createEmptyBorder(11, 22, 11, 22));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    encima = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    encima = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (!isEnabled()) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
            }
            boolean presionado = getModel().isPressed();
            boolean activo = encima && isEnabled();
            if (estilo == PRIMARIO) {
                g2.setColor(presionado ? Estilos.BOTON_PRESIONADO : activo ? Estilos.BOTON_HOVER : Estilos.BOTON);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, RADIO, RADIO);
            } else {
                g2.setColor(presionado ? Estilos.FONDO : activo ? new Color(16, 55, 92) : Estilos.CAMPO);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, RADIO, RADIO);
                g2.setColor(Estilos.BORDE);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, RADIO, RADIO);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class CajaRedondeada extends JPanel {

        CajaRedondeada(LayoutManager layout) {
            super(layout);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Estilos.CAMPO);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, RADIO, RADIO);
            g2.setColor(Estilos.BORDE);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, RADIO, RADIO);
            g2.dispose();
        }
    }

    // Buscador con el estilo de los campos del login y texto de ayuda.
    private static class CampoBusqueda extends JTextField {

        private final String ayuda;

        CampoBusqueda(String ayuda) {
            this.ayuda = ayuda;
            setOpaque(false);
            setFont(Estilos.NORMAL);
            setForeground(Estilos.TEXTO);
            setCaretColor(Estilos.TEXTO);
            setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            g2.setColor(Estilos.CAMPO);
            g2.fillRoundRect(0, 0, w - 1, h - 1, RADIO, RADIO);
            g2.setColor(hasFocus() ? Estilos.BOTON_HOVER : Estilos.BORDE);
            g2.drawRoundRect(0, 0, w - 1, h - 1, RADIO, RADIO);

            if (getText().isEmpty()) {
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(ayuda, getInsets().left, (h - fm.getHeight()) / 2 + fm.getAscent());
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // Combo para el filtro de género, con el estilo de los campos del login.
    private static class ComboOscuro<E> extends JComboBox<E> {

        ComboOscuro() {
            setUI(new BasicComboBoxUI() {
                @Override
                protected JButton createArrowButton() {
                    JButton flecha = new JButton() {
                        @Override
                        protected void paintComponent(Graphics g) {
                            Graphics2D g2 = (Graphics2D) g.create();
                            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            g2.setColor(Estilos.TEXTO_SUAVE);
                            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                            int cx = getWidth() / 2 - 4;
                            int cy = getHeight() / 2 - 1;
                            g2.drawPolyline(new int[]{cx - 5, cx, cx + 5}, new int[]{cy - 2, cy + 3, cy - 2}, 3);
                            g2.dispose();
                        }
                    };
                    flecha.setContentAreaFilled(false);
                    flecha.setBorderPainted(false);
                    flecha.setFocusPainted(false);
                    flecha.setOpaque(false);
                    return flecha;
                }

                @Override
                public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                    // el fondo lo pinta el combo
                }

                @Override
                public void paintCurrentValue(Graphics g, Rectangle bounds, boolean hasFocus) {
                    super.paintCurrentValue(g, bounds, false);
                }
            });

            setOpaque(false);
            setFont(Estilos.NORMAL);
            setForeground(Estilos.TEXTO);
            setBackground(Estilos.CAMPO);
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 4));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                        boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (index == -1) {
                        setOpaque(false);
                        setForeground(Estilos.TEXTO);
                        setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
                    } else {
                        setOpaque(true);
                        setBackground(isSelected ? Estilos.BOTON : Estilos.CAMPO);
                        setForeground(Estilos.TEXTO);
                        setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
                    }
                    return this;
                }
            });

            Object popup = getUI().getAccessibleChild(this, 0);
            if (popup instanceof JPopupMenu menu) {
                menu.setBorder(BorderFactory.createLineBorder(Estilos.BORDE));
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Estilos.CAMPO);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, RADIO, RADIO);
            g2.setColor(isPopupVisible() ? Estilos.BOTON_HOVER : Estilos.BORDE);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, RADIO, RADIO);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // Barra de desplazamiento delgada, sin flechas, que combina con el fondo oscuro.
    private static class BarraDesplazamiento extends BasicScrollBarUI {

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return botonInvisible();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return botonInvisible();
        }

        private static JButton botonInvisible() {
            JButton boton = new JButton();
            boton.setPreferredSize(new Dimension(0, 0));
            boton.setMinimumSize(new Dimension(0, 0));
            boton.setMaximumSize(new Dimension(0, 0));
            return boton;
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            // pista transparente
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            if (r.isEmpty() || !scrollbar.isEnabled()) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(255, 255, 255, isThumbRollover() ? 110 : 60));
            g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 8, 8);
            g2.dispose();
        }
    }

    // Panel que se ajusta al ancho visible del scroll y solo se desplaza en vertical.
    private static class PanelDesplazable extends JPanel implements Scrollable {

        PanelDesplazable(LayoutManager layout) {
            super(layout);
            setOpaque(false);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visible, int orientacion, int direccion) {
            return 24;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visible, int orientacion, int direccion) {
            return visible.height - 48;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }
}
