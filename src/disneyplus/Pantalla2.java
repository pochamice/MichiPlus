package disneyplus;

import javax.swing.*;
import java.awt.*;

public class Pantalla2 extends javax.swing.JFrame {

    // Diseño
    public static class Estilos {

        public static final Color AZUL_FONDO = new Color(5, 18, 38);
        public static final Color AZUL_DISNEY = new Color(0, 99, 229);
        public static final Color VERDE_AZULADO = new Color(14, 98, 117);
        public static final Color BOTON_NEGRO = new Color(37, 38, 44);
        public static final Font TITULO = new Font("Segoe UI", Font.BOLD, 22);
        public static final Font NORMAL = new Font("Segoe UI", Font.PLAIN, 16);
        public static final Color CAMPO_GRIS = new Color(232, 233, 237);
        public static final Color LINEA_CAMPO = new Color(30, 30, 35);
        public static final Color ETIQUETA_CAMPO = new Color(100, 102, 110);
        public static final Color TEXTO_CAMPO = new Color(20, 20, 25);
        public static final int RADIO_CAMPO = 8;

    }

    public static class PanelDegradado extends javax.swing.JPanel {

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            super.paintComponent(g);
            java.awt.Graphics2D g2d = (java.awt.Graphics2D) g.create();
            int w = getWidth();
            int h = getHeight();

            float[] fracciones = {0.0f, 0.5f, 1.0f};
            Color[] colores = {
                Estilos.AZUL_FONDO,
                Estilos.AZUL_DISNEY,
                Estilos.VERDE_AZULADO
            };

            java.awt.LinearGradientPaint lgp = new java.awt.LinearGradientPaint(new java.awt.geom.Point2D.Float(0, 0),
                    new java.awt.geom.Point2D.Float(w, h), fracciones, colores);
            g2d.setPaint(lgp);
            g2d.fillRect(0, 0, w, h);
            g2d.dispose();
        }
    }

    public static class PanelRedondeado extends javax.swing.JPanel {

        public PanelRedondeado() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {

            java.awt.Graphics2D g2d = (java.awt.Graphics2D) g.create();
            g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(getBackground());
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
            g2d.dispose();
            super.paintComponent(g);
        }
    }

    public static class BotonRedondeado extends javax.swing.JButton {

        private boolean hover = false;
        private final int radio = 30;

        public BotonRedondeado(String texto) {
            super(texto);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setForeground(Color.WHITE);
            setFont(Estilos.NORMAL.deriveFont(Font.BOLD));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    hover = true;
                    repaint();
                }

                public void mouseExited(java.awt.event.MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color base = Estilos.BOTON_NEGRO;
                if (getModel().isPressed()) {
                    base = base.darker();
                } else if (hover) {
                    base = base.brighter();
                }

                g2.setColor(base);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), radio, radio);
                g2.dispose();

                super.paintComponent(g);

            }

        }

    // Fondo gris redondeado + línea inferior (azul con foco). Lo comparten todos los campos.
    private static void pintarFondoCampo(Graphics g, JComponent c, boolean foco) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = c.getWidth();
        int h = c.getHeight();
        int r = Estilos.RADIO_CAMPO;
        int grosor = foco ? 3 : 2;

        g2.setColor(foco ? Estilos.AZUL_DISNEY : Estilos.LINEA_CAMPO);
        g2.fillRoundRect(0, h - r * 2, w, r * 2, r, r);

        g2.setColor(Estilos.CAMPO_GRIS);
        g2.fillRoundRect(0, 0, w, h - grosor, r, r);

        g2.dispose();
    }

    private static java.awt.event.FocusAdapter repintarConFoco(Component c) {
        return new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                c.repaint();
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                c.repaint();
            }
        };
    }

    public static class CampoTexto extends javax.swing.JTextField {

        private final String etiqueta;

        public CampoTexto(String etiqueta) {
            this.etiqueta = etiqueta;
            setOpaque(false);
            setFont(Estilos.NORMAL);
            setForeground(Estilos.TEXTO_CAMPO);
            setCaretColor(Estilos.TEXTO_CAMPO);
            // abajo deja espacio para la línea
            setBorder(BorderFactory.createEmptyBorder(0, 15, 2, 15));
            addFocusListener(repintarConFoco(this));
        }

        @Override
        protected void paintComponent(Graphics g) {
            pintarFondoCampo(g, this, hasFocus());
            super.paintComponent(g);

            // la etiqueta funciona como texto de ayuda mientras el campo está vacío
            if (getText().isEmpty() && !hasFocus()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setFont(getFont());
                g2.setColor(Estilos.ETIQUETA_CAMPO);
                FontMetrics fm = g2.getFontMetrics();
                int y = (getHeight() - getInsets().bottom - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(etiqueta, getInsets().left, y);
                g2.dispose();
            }
        }
    }

    public static class ComboCampo<E> extends javax.swing.JComboBox<E> {

        public ComboCampo() {
            setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
                @Override
                protected JButton createArrowButton() {
                    JButton flecha = new JButton() {
                        @Override
                        protected void paintComponent(Graphics g) {
                            Graphics2D g2 = (Graphics2D) g.create();
                            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            g2.setColor(comboBox.hasFocus() ? Estilos.AZUL_DISNEY : Estilos.ETIQUETA_CAMPO);
                            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                            int cx = getWidth() / 2;
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
                    // el fondo ya lo pinta pintarFondoCampo
                }

                @Override
                public void paintCurrentValue(Graphics g, Rectangle bounds, boolean hasFocus) {
                    // sin el resaltado azul de selección: el foco se indica con la línea inferior
                    super.paintCurrentValue(g, bounds, false);
                }
            });

            setOpaque(false);
            setFont(Estilos.NORMAL);
            setForeground(Estilos.TEXTO_CAMPO);
            setBackground(Estilos.CAMPO_GRIS);
            setBorder(BorderFactory.createEmptyBorder(0, 10, 2, 4));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                        boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (index == -1) {
                        // valor mostrado dentro del combo
                        setOpaque(false);
                        setForeground(Estilos.TEXTO_CAMPO);
                        setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
                    } else {
                        // opciones de la lista desplegable
                        setOpaque(true);
                        setBackground(isSelected ? Estilos.AZUL_DISNEY : Color.WHITE);
                        setForeground(isSelected ? Color.WHITE : Estilos.TEXTO_CAMPO);
                        setBorder(BorderFactory.createEmptyBorder(6, 15, 6, 15));
                    }
                    return this;
                }
            });

            Object popup = getUI().getAccessibleChild(this, 0);
            if (popup instanceof JPopupMenu) {
                ((JPopupMenu) popup).setBorder(BorderFactory.createLineBorder(Estilos.CAMPO_GRIS));
            }

            addFocusListener(repintarConFoco(this));
        }

        @Override
        protected void paintComponent(Graphics g) {
            pintarFondoCampo(g, this, hasFocus());
            super.paintComponent(g);
        }
    }

    public static class AreaCampo extends javax.swing.JScrollPane {

        public AreaCampo() {
            setOpaque(false);
            getViewport().setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(10, 15, 6, 6));
            setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
        }

        @Override
        public void setViewportView(Component vista) {
            super.setViewportView(vista);
            if (vista instanceof JComponent) {
                ((JComponent) vista).setOpaque(false);
                vista.addFocusListener(repintarConFoco(this));
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Component vista = getViewport().getView();
            pintarFondoCampo(g, this, vista != null && vista.hasFocus());
            super.paintComponent(g);
        }
    }

        // Funcionamiento
        private String rutaPortada = "";

        public void elegirPortada() {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Imágenes (jpg, png)", "jpg", "jpeg", "png"));

            if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
                return;
            }

            rutaPortada = fc.getSelectedFile().getAbsolutePath();
            ImageIcon original = new ImageIcon(rutaPortada);

            int maxW = lblpreview.getWidth();
            int maxH = lblpreview.getHeight();
            double escala = Math.min((double) maxW / original.getIconWidth(), (double) maxH / original.getIconHeight());
            int w = (int) (original.getIconWidth() * escala);
            int h = (int) (original.getIconHeight() * escala);

            Image img = original.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
            lblpreview.setIcon(new ImageIcon(img));
            lblpreview.setText("");

        }

        public static class Contenido {

            // Única colección de la aplicación. Es estática, así que sobrevive al cerrar y abrir
            // pantallas: Pantalla2 agrega aquí y Pantalla3 lee y recorre esta misma lista.
            public static final ListaCircularDoble CATALOGO = new ListaCircularDoble();

            private final String nombre;
            private final String genero;
            private final String tipo;
            private final String sinopsis;
            private final String portada;
            private final int anio;
            private int cantidad;

            public Contenido(String nombre, String genero, String tipo, String sinopsis, String portada,
                    int anio, int cantidad) {
                this.nombre = nombre;
                this.genero = genero;
                this.tipo = tipo;
                this.sinopsis = sinopsis;
                this.portada = portada;
                this.anio = anio;
                this.cantidad = cantidad;
            }

            public String getNombre() {
                return nombre;
            }

            public String getGenero() {
                return genero;
            }

            public String getTipo() {
                return tipo;
            }

            public String getSinopsis() {
                return sinopsis;
            }

            public String getPortada() {
                return portada;
            }

            public int getAnio() {
                return anio;
            }

            public int getCantidad() {
                return cantidad;
            }

            public void setCantidad(int cantidad) {
                this.cantidad = Math.max(0, cantidad);
            }

            // esto tambien se va a poder editar!!!!!!! asegurate de añadir la sinopsis tmb
            @Override
            public String toString() {
                return tipo + ": " + nombre + " (" + genero + ")";

            }

        }

        private void guardarYcontinuar() {
            String nombre = TxTNombre.getText().trim();
            String genero = (String) ComBoxGen.getSelectedItem();
            String tipo = (String) ComBoxSrsPeli.getSelectedItem();
            String sinopsis = TxtAR_Desc.getText().trim();

            if (nombre.isEmpty() || sinopsis.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Por favor llena el nombre y la sinopsis.",
                        "Faltan datos", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int anioMaximo = java.time.Year.now().getValue() + 5;
            Integer anio = leerEntero(TxtAnio, 1888, anioMaximo,
                    "Escribe un año válido (entre 1888 y " + anioMaximo + ").");
            if (anio == null) {
                return;
            }
            Integer cantidad = leerEntero(TxtCantidad, 0, 100000,
                    "Escribe una cantidad válida de unidades (0 o más).");
            if (cantidad == null) {
                return;
            }

            // si el título ya está en el inventario, se ofrece sumar las unidades en lugar de duplicarlo
            Contenido existente = Contenido.CATALOGO.buscar(nombre);
            if (existente != null) {
                int respuesta = JOptionPane.showConfirmDialog(this,
                        "\"" + existente.getNombre() + "\" ya está en el inventario con "
                        + existente.getCantidad() + " unidades.\n¿Quieres sumarle " + cantidad + " unidades?",
                        "Título repetido", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (respuesta != JOptionPane.YES_OPTION) {
                    return;
                }
                existente.setCantidad(existente.getCantidad() + cantidad);
                abrirCatalogo(existente);
                return;
            }

            Contenido nuevo = new Contenido(nombre, genero, tipo, sinopsis, rutaPortada, anio, cantidad);
            Contenido.CATALOGO.agregar(nuevo);
            abrirCatalogo(nuevo);
        }

        // Devuelve el número escrito en el campo o null (y avisa) si no es válido.
        private Integer leerEntero(JTextField campo, int minimo, int maximo, String mensaje) {
            try {
                int valor = Integer.parseInt(campo.getText().trim());
                if (valor >= minimo && valor <= maximo) {
                    return valor;
                }
            } catch (NumberFormatException e) {
                // se avisa abajo
            }
            JOptionPane.showMessageDialog(this, mensaje, "Dato no válido", JOptionPane.WARNING_MESSAGE);
            campo.requestFocusInWindow();
            return null;
        }

        private void abrirCatalogo(Contenido resaltado) {
            Pantalla3 p3 = new Pantalla3(resaltado);
            p3.setVisible(true);
            this.dispose();
        }

        public Pantalla2() {
            initComponents();

            setTitle("Gestor de almacenamiento");
            setLocationRelativeTo(null);
            pantallaCompleta();
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            PanelBlanco.setBackground(new Color(255, 255, 255));

            LblBienvenida.setFont(Estilos.TITULO);
            LblBienvenida.setText("Bienvenido al gestor de peliculas y series.");

            LblNombre.setFont(Estilos.NORMAL);
            LblNombre.setText("Ingrese el nombre:");

            LblGenero.setFont(Estilos.NORMAL);
            LblGenero.setText("Ingrese el genero:");

            Lbl_Info.setFont(Estilos.NORMAL);
            Lbl_Info.setText("Añada los datos de la pelicula o serie que desea agregar al catálogo.");

            LblSrsPeli.setFont(Estilos.NORMAL);
            LblSrsPeli.setText("¿Serie o Pelicula?");

            LblAnioCant.setFont(Estilos.NORMAL);
            LblAnioCant.setText("Año y cantidad:");

            LblDesc.setFont(Estilos.NORMAL);
            LblDesc.setText("Sinopsis:");

            LblImagen.setFont(Estilos.NORMAL);
            LblImagen.setText("Añade la imagen enlazada:");

            lblinfocontacto.setFont(Estilos.NORMAL);
            lblinfocontacto.setForeground(new Color(14, 98, 117));
            lblinfocontacto.setText("¿Algun problema? Contáctanos.");

            ComBoxSrsPeli.setFont(Estilos.NORMAL);
            ComBoxGen.setFont(Estilos.NORMAL);
            TxTNombre.setFont(Estilos.NORMAL);
            TxtAR_Desc.setFont(Estilos.NORMAL);
            TxtAR_Desc.setForeground(Estilos.TEXTO_CAMPO);
            TxtAR_Desc.setCaretColor(Estilos.TEXTO_CAMPO);
            TxtAR_Desc.setLineWrap(true);
            TxtAR_Desc.setWrapStyleWord(true);

            btnAniadir.addActionListener(e -> guardarYcontinuar());
            btnImagen.addActionListener(e -> elegirPortada());
            btnCatalogo.addActionListener(e -> abrirCatalogo(null));

        }

        private void pantallaCompleta() {
            setExtendedState(JFrame.MAXIMIZED_BOTH);
            addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowOpened(java.awt.event.WindowEvent e) {
                    setExtendedState(JFrame.MAXIMIZED_BOTH);
                }
            });
        }

        @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        PanelPrincipal = new PanelDegradado();
        iconoDisney = new javax.swing.JLabel();
        PanelBlanco = new PanelRedondeado();
        LblBienvenida = new javax.swing.JLabel();
        LblGenero = new javax.swing.JLabel();
        TxTNombre = new CampoTexto("Nombre");
        LblNombre = new javax.swing.JLabel();
        ComBoxGen = new ComboCampo<>();
        btnAniadir = new BotonRedondeado("Añadir");
        Lbl_Info = new javax.swing.JLabel();
        LblSrsPeli = new javax.swing.JLabel();
        ComBoxSrsPeli = new ComboCampo<>();
        LblDesc = new javax.swing.JLabel();
        jScrollPane1 = new AreaCampo();
        TxtAR_Desc = new javax.swing.JTextArea();
        LblImagen = new javax.swing.JLabel();
        lblinfocontacto = new javax.swing.JLabel();
        lblpreview = new javax.swing.JLabel();
        btnImagen = new BotonRedondeado("Elegir portada");
        LblAnioCant = new javax.swing.JLabel();
        TxtAnio = new CampoTexto("Año");
        TxtCantidad = new CampoTexto("Cantidad");
        btnCatalogo = new BotonRedondeado("Ver catálogo");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        PanelPrincipal.setPreferredSize(new java.awt.Dimension(1920, 1080));

        iconoDisney.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/michi_plus_1.png"))); // NOI18N
        iconoDisney.setText("jLabel1");

        LblBienvenida.setText("LblTitulo");

        LblGenero.setText("jLabel1");

        TxTNombre.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TxTNombreActionPerformed(evt);
            }
        });

        LblNombre.setText("jLabel1");

        ComBoxGen.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Romance", "Infantil", "Terror", "Acción", "Comedia", "Novela", "Documental" }));

        btnAniadir.setText("Añadir");

        Lbl_Info.setText("jLabel1");

        LblSrsPeli.setText("jLabel1");

        ComBoxSrsPeli.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Pelicula", "Serie" }));

        LblDesc.setText("jLabel1");

        TxtAR_Desc.setColumns(20);
        TxtAR_Desc.setRows(5);
        jScrollPane1.setViewportView(TxtAR_Desc);

        LblImagen.setText("jLabel1");

        lblinfocontacto.setText("jLabel1");

        lblpreview.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblpreview.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        lblpreview.setMaximumSize(new java.awt.Dimension(140, 200));
        lblpreview.setMinimumSize(new java.awt.Dimension(100, 200));

        btnImagen.setText("Elegir portada");

        LblAnioCant.setText("jLabel1");

        btnCatalogo.setText("Ver catálogo");

        javax.swing.GroupLayout PanelBlancoLayout = new javax.swing.GroupLayout(PanelBlanco);
        PanelBlanco.setLayout(PanelBlancoLayout);
        PanelBlancoLayout.setHorizontalGroup(
            PanelBlancoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelBlancoLayout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addGroup(PanelBlancoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(LblBienvenida)
                    .addComponent(Lbl_Info)
                    .addGroup(PanelBlancoLayout.createSequentialGroup()
                        .addGroup(PanelBlancoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(LblNombre, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(LblGenero, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(LblSrsPeli, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(LblAnioCant, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(LblDesc, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(10, 10, 10)
                        .addGroup(PanelBlancoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(TxTNombre, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(ComBoxGen, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(ComBoxSrsPeli, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(PanelBlancoLayout.createSequentialGroup()
                                .addComponent(TxtAnio, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(10, 10, 10)
                                .addComponent(TxtCantidad, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(50, 50, 50)
                        .addGroup(PanelBlancoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(LblImagen)
                            .addComponent(lblpreview, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnImagen, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(PanelBlancoLayout.createSequentialGroup()
                        .addComponent(btnAniadir, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(15, 15, 15)
                        .addComponent(btnCatalogo, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lblinfocontacto))
                .addGap(40, 40, 40))
        );
        PanelBlancoLayout.setVerticalGroup(
            PanelBlancoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelBlancoLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(LblBienvenida, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(Lbl_Info, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addGroup(PanelBlancoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(PanelBlancoLayout.createSequentialGroup()
                        .addGap(32, 32, 32)
                        .addGroup(PanelBlancoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(LblNombre, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(TxTNombre, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(PanelBlancoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(LblGenero, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(ComBoxGen, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(PanelBlancoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(LblSrsPeli, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(ComBoxSrsPeli, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(PanelBlancoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(LblAnioCant, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(TxtAnio, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(TxtCantidad, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(PanelBlancoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(LblDesc, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(PanelBlancoLayout.createSequentialGroup()
                        .addComponent(LblImagen, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8, 8, 8)
                        .addComponent(lblpreview, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(btnImagen, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(20, 20, 20)
                .addGroup(PanelBlancoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnAniadir, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCatalogo, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8, 8, 8)
                .addComponent(lblinfocontacto, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15))
        );

        javax.swing.GroupLayout PanelPrincipalLayout = new javax.swing.GroupLayout(PanelPrincipal);
        PanelPrincipal.setLayout(PanelPrincipalLayout);
        PanelPrincipalLayout.setHorizontalGroup(
            PanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelPrincipalLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(PanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(iconoDisney, javax.swing.GroupLayout.PREFERRED_SIZE, 395, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(PanelBlanco, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        PanelPrincipalLayout.setVerticalGroup(
            PanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelPrincipalLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(iconoDisney, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(PanelBlanco, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(PanelPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(PanelPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void TxTNombreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TxTNombreActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TxTNombreActionPerformed

        /**
         * @param args the command line arguments
         */
        public static void main(String args[]) {

            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    new Pantalla2().setVisible(true);
                }
            });
        }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> ComBoxGen;
    private javax.swing.JComboBox<String> ComBoxSrsPeli;
    private javax.swing.JLabel LblAnioCant;
    private javax.swing.JLabel LblBienvenida;
    private javax.swing.JLabel LblDesc;
    private javax.swing.JLabel LblGenero;
    private javax.swing.JLabel LblImagen;
    private javax.swing.JLabel LblNombre;
    private javax.swing.JLabel LblSrsPeli;
    private javax.swing.JLabel Lbl_Info;
    private javax.swing.JPanel PanelBlanco;
    private javax.swing.JPanel PanelPrincipal;
    private javax.swing.JTextField TxTNombre;
    private javax.swing.JTextArea TxtAR_Desc;
    private javax.swing.JTextField TxtAnio;
    private javax.swing.JTextField TxtCantidad;
    private javax.swing.JButton btnAniadir;
    private javax.swing.JButton btnCatalogo;
    private javax.swing.JButton btnImagen;
    private javax.swing.JLabel iconoDisney;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblinfocontacto;
    private javax.swing.JLabel lblpreview;
    // End of variables declaration//GEN-END:variables
}
