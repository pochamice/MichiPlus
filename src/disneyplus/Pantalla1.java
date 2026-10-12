/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package disneyplus;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class Pantalla1 extends JFrame {

    // ===== Configuración =====
    private static final String IMAGEN_FONDO = "/imagenes/uno.png";
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", Locale.of("es", "MX"));
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    // ===== Estilo =====
    private static final int RADIO = 20;
    private static final Color COLOR_CAMPO = new Color(10, 40, 70);
    private static final Color COLOR_BORDE = new Color(60, 120, 180);
    private static final Color COLOR_BOTON = new Color(0, 114, 210);
    private static final Color COLOR_BOTON_HOVER = new Color(0, 140, 240);
    private static final Color COLOR_BOTON_PRESIONADO = new Color(0, 85, 160);
    private static final Color COLOR_BOTON_DESHABILITADO = new Color(50, 85, 125);
    private static final Color COLOR_TEXTO_SUAVE = new Color(170, 200, 230);

    // ===== Sesión actual (la pantalla siguiente puede leerla) =====
    private static String usuarioActual;
    private static String nombreActual;

    public static String getUsuarioActual() { return usuarioActual; }
    public static String getNombreActual() { return nombreActual; }

    // Cierra la ventana recibida, borra la sesión y vuelve a mostrar el login.
    // El botón "Cerrar sesión" del catálogo solo tiene que llamar: Pantalla1.cerrarSesion(this);
    public static void cerrarSesion(Window ventanaActual) {
        usuarioActual = null;
        nombreActual = null;
        if (ventanaActual != null) {
            ventanaActual.dispose();
        }
        SwingUtilities.invokeLater(() -> new Pantalla1().setVisible(true));
    }

    // ===== Ejecutivos =====
    private static class Ejecutivo {
        final String nombre, password;
        Ejecutivo(String nombre, String password) {
            this.nombre = nombre;
            this.password = password;
        }
    }

    private final Map<String, Ejecutivo> ejecutivos = new LinkedHashMap<>();

    // ===== Componentes =====
    private FondoPanel fondo;
    private CampoTexto txtUsuario;
    private CampoPassword txtPassword;
    private BotonOjo btnOjo;
    private BotonRedondeado btnIniciar;
    private JLabel lblFecha;
    private JLabel lblHora;
    private javax.swing.Timer relojTimer;

    private char ecoOculto;
    private boolean mostrandoContrasena = false;
    private boolean procesando = false;

    public Pantalla1() {
        // Credenciales de demostración escolar (no son seguras)
        ejecutivos.put("ejecutivo1", new Ejecutivo("Ejecutivo Uno", "Disney#01"));
        ejecutivos.put("ejecutivo2", new Ejecutivo("Ejecutivo Dos", "Disney#02"));

        setTitle("Disney+ - Portal de empleados");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 520));
        // Mismo tamaño previo que Pantalla2 y Pantalla3: con 125 % de escala, una ventana más chica
        // que la pantalla deja sin pintar la última columna al maximizarse (línea blanca).
        setSize(1920, 1080);
        setLocationRelativeTo(null);

        fondo = new FondoPanel();
        fondo.setLayout(null);
        setContentPane(fondo);

        txtUsuario = new CampoTexto();
        txtPassword = new CampoPassword();
        ecoOculto = txtPassword.getEchoChar();
        btnOjo = new BotonOjo();
        btnIniciar = new BotonRedondeado("Iniciar sesión");
        lblFecha = new JLabel("", SwingConstants.RIGHT);
        lblHora = new JLabel("", SwingConstants.RIGHT);
        lblFecha.setForeground(COLOR_TEXTO_SUAVE);
        lblHora.setForeground(Color.WHITE);

        // El ojo se agrega primero para que quede por encima del campo de contraseña
        fondo.add(btnOjo);
        fondo.add(txtPassword);
        fondo.add(txtUsuario);
        fondo.add(btnIniciar);
        fondo.add(lblFecha);
        fondo.add(lblHora);

        // Reacomoda los componentes cada vez que cambia el tamaño de la ventana
        fondo.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                acomodar();
            }
        });

        btnIniciar.addActionListener(e -> validarLogin());
        txtPassword.addActionListener(e -> validarLogin());                  // Enter en contraseña
        txtUsuario.addActionListener(e -> txtPassword.requestFocusInWindow()); // Enter en usuario
        btnOjo.addActionListener(e -> alternarContrasena());

        // Reloj: se actualiza cada segundo y se detiene al cerrar la ventana
        relojTimer = new javax.swing.Timer(1000, e -> actualizarReloj());
        actualizarReloj();
        relojTimer.start();

        setExtendedState(JFrame.MAXIMIZED_BOTH); // arranca en pantalla completa
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                setExtendedState(JFrame.MAXIMIZED_BOTH);
            }
        });
    }

    @Override
    public void dispose() {
        if (relojTimer != null) {
            relojTimer.stop();
        }
        super.dispose();
    }

    // Posiciones y tamaños en proporción al tamaño de la ventana
    private void acomodar() {
        int w = fondo.getWidth();
        int h = fondo.getHeight();
        if (w == 0 || h == 0) return;

        int campoX = (int) (w * 0.013);
        int campoW = (int) (w * 0.38);
        int campoH = (int) (h * 0.058);
        int userY = (int) (h * 0.506);
        int passY = (int) (h * 0.689);

        txtUsuario.setBounds(campoX, userY, campoW, campoH);
        txtPassword.setBounds(campoX, passY, campoW, campoH);
        btnIniciar.setBounds(campoX, (int) (h * 0.800), campoW, (int) (h * 0.062));

        // Ojo dentro del campo de contraseña, alineado a la derecha
        int ojo = (int) (campoH * 0.65);
        btnOjo.setBounds(campoX + campoW - ojo - (int) (campoH * 0.2),
                passY + (campoH - ojo) / 2, ojo, ojo);

        // Fecha y hora en la barra superior, a la izquierda de "STAFF ACCESS"
        lblFecha.setBounds((int) (w * 0.50), (int) (h * 0.032), (int) (w * 0.315), (int) (h * 0.040));
        lblHora.setBounds((int) (w * 0.50), (int) (h * 0.070), (int) (w * 0.315), (int) (h * 0.050));

        Font fuente = new Font("Segoe UI", Font.PLAIN, Math.max(14, h / 45));
        txtUsuario.setFont(fuente);
        txtPassword.setFont(fuente);
        btnIniciar.setFont(fuente.deriveFont(Font.BOLD));
        lblFecha.setFont(new Font("Segoe UI", Font.PLAIN, Math.max(12, h / 58)));
        lblHora.setFont(new Font("Segoe UI", Font.BOLD, Math.max(16, h / 38)));
    }

    // ===== Reloj =====
    private void actualizarReloj() {
        LocalDateTime ahora = LocalDateTime.now();
        String fecha = ahora.format(FORMATO_FECHA);
        lblFecha.setText(Character.toUpperCase(fecha.charAt(0)) + fecha.substring(1));
        lblHora.setText(ahora.format(FORMATO_HORA));
    }

    // ===== Mostrar / ocultar contraseña =====
    private void alternarContrasena() {
        mostrandoContrasena = !mostrandoContrasena;
        txtPassword.setEchoChar(mostrandoContrasena ? (char) 0 : ecoOculto);
        btnOjo.setMostrando(mostrandoContrasena);
        txtPassword.requestFocusInWindow();
    }

    // ===== Inicio de sesión =====
    private void validarLogin() {
        if (procesando) return; // evita abrir varias ventanas por clics repetidos

        String usuario = txtUsuario.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (usuario.isEmpty() && password.isEmpty()) {
            avisar("Ingresa tu usuario y tu contraseña para continuar.");
            txtUsuario.requestFocusInWindow();
            return;
        }
        if (usuario.isEmpty()) {
            avisar("Falta el usuario. Escríbelo para continuar.");
            txtUsuario.requestFocusInWindow();
            return;
        }
        if (password.isEmpty()) {
            avisar("Falta la contraseña. Escríbela para continuar.");
            txtPassword.requestFocusInWindow();
            return;
        }

        Ejecutivo ej = ejecutivos.get(usuario);
        if (ej == null || !ej.password.equals(password)) {
            JOptionPane.showMessageDialog(this,
                    "Acceso denegado.\nEl usuario o la contraseña son incorrectos.",
                    "Acceso denegado", JOptionPane.ERROR_MESSAGE);
            txtPassword.setText("");
            txtPassword.requestFocusInWindow();
            return;
        }

        // Credenciales correctas: guardamos la sesión y bloqueamos el botón
        usuarioActual = usuario;
        nombreActual = ej.nombre;
        setProcesando(true);

        JOptionPane.showMessageDialog(this, "¡Bienvenido, " + ej.nombre + "!",
                "Acceso concedido", JOptionPane.INFORMATION_MESSAGE);

        abrirCatalogo();
    }

    private void avisar(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Datos incompletos", JOptionPane.WARNING_MESSAGE);
    }

    private void setProcesando(boolean valor) {
        procesando = valor;
        btnIniciar.setEnabled(!valor);
        btnIniciar.setText(valor ? "Ingresando..." : "Iniciar sesión");
    }

    private void abrirCatalogo() {
        // El nombre del ejecutivo se puede leer desde Pantalla2 con Pantalla1.getNombreActual()
        new Pantalla2().setVisible(true);
        dispose(); // cierra el login y detiene el reloj
    }

    // ===== Dibujo de componentes redondeados =====
    private static void pintarFondoRedondeado(Graphics g, JComponent c, Color color) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.fillRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, RADIO, RADIO);
        g2.dispose();
    }

    private static void pintarBordeRedondeado(Graphics g, JComponent c) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(COLOR_BORDE);
        g2.drawRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, RADIO, RADIO);
        g2.dispose();
    }

    private static class CampoTexto extends JTextField {
        CampoTexto() {
            setOpaque(false);
            setForeground(Color.WHITE);
            setCaretColor(Color.WHITE);
            setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        }
        @Override protected void paintComponent(Graphics g) {
            pintarFondoRedondeado(g, this, COLOR_CAMPO);
            super.paintComponent(g);
        }
        @Override protected void paintBorder(Graphics g) {
            pintarBordeRedondeado(g, this);
        }
    }

    private static class CampoPassword extends JPasswordField {
        CampoPassword() {
            setOpaque(false);
            setForeground(Color.WHITE);
            setCaretColor(Color.WHITE);
            // Margen derecho mayor para que el texto no quede debajo del ojo
            setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 50));
        }
        @Override protected void paintComponent(Graphics g) {
            pintarFondoRedondeado(g, this, COLOR_CAMPO);
            super.paintComponent(g);
        }
        @Override protected void paintBorder(Graphics g) {
            pintarBordeRedondeado(g, this);
        }
    }

    private static class BotonRedondeado extends JButton {
        private boolean encima = false;

        BotonRedondeado(String texto) {
            super(texto);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { encima = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { encima = false; repaint(); }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            Color color;
            if (!isEnabled()) color = COLOR_BOTON_DESHABILITADO;
            else if (getModel().isPressed()) color = COLOR_BOTON_PRESIONADO;
            else if (encima) color = COLOR_BOTON_HOVER;
            else color = COLOR_BOTON;
            pintarFondoRedondeado(g, this, color);

            // Texto centrado (baja 1 px al presionar)
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            if (isEnabled() && getModel().isPressed()) y += 1;
            g2.setColor(isEnabled() ? Color.WHITE : new Color(190, 205, 220));
            g2.drawString(getText(), x, y);
            g2.dispose();
        }
    }

    // Botón con icono de ojo dibujado por código (no necesita imágenes)
    private static class BotonOjo extends JButton {
        private boolean encima = false;
        private boolean mostrando = false;

        BotonOjo() {
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setFocusable(false); // no le quita el foco al campo de contraseña
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setToolTipText("Mostrar contraseña");
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { encima = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { encima = false; repaint(); }
            });
        }

        void setMostrando(boolean valor) {
            mostrando = valor;
            setToolTipText(valor ? "Ocultar contraseña" : "Mostrar contraseña");
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(encima ? Color.WHITE : COLOR_TEXTO_SUAVE);
            g2.setStroke(new BasicStroke(Math.max(1.5f, getHeight() / 14f),
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            float w = getWidth();
            float h = getHeight();
            float ancho = w * 0.80f;
            float alto = h * 0.38f;
            float x0 = (w - ancho) / 2f;
            float x1 = x0 + ancho;
            float cy = h / 2f;

            // Contorno del ojo
            Path2D.Float ojo = new Path2D.Float();
            ojo.moveTo(x0, cy);
            ojo.quadTo(w / 2f, cy - alto * 2f, x1, cy);
            ojo.quadTo(w / 2f, cy + alto * 2f, x0, cy);
            g2.draw(ojo);

            // Pupila
            float r = alto * 0.55f;
            g2.fill(new java.awt.geom.Ellipse2D.Float(w / 2f - r, cy - r, r * 2, r * 2));

            // Contraseña oculta: ojo tachado
            if (!mostrando) {
                g2.drawLine((int) (x0 + ancho * 0.12f), (int) (cy - alto * 1.3f),
                        (int) (x0 + ancho * 0.88f), (int) (cy + alto * 1.3f));
            }
            g2.dispose();
        }
    }

    // ===== Fondo =====
    private class FondoPanel extends JPanel {
        private Image imagen;

        FondoPanel() {
            // Si algún borde queda sin cubrir por la imagen, se ve azul oscuro y no gris claro
            setBackground(new Color(0, 13, 40));
            java.net.URL url = getClass().getResource(IMAGEN_FONDO);
            if (url == null) {
                System.err.println("No se encontró " + IMAGEN_FONDO
                        + ". Revisa que esté en src/imagenes/");
            } else {
                imagen = quitarFranjaBlanca(url);
            }
        }

        // uno.png trae una franja blanca en la parte de abajo; se recorta antes de dibujarla
        private Image quitarFranjaBlanca(java.net.URL url) {
            try {
                java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(url);
                int alto = img.getHeight();
                while (alto > img.getHeight() / 2 && filaClara(img, alto - 1)) {
                    alto--;
                }
                return img.getSubimage(0, 0, img.getWidth(), alto);
            } catch (java.io.IOException e) {
                return new ImageIcon(url).getImage();
            }
        }

        // Una fila es "clara" si al menos 40 % de sus pixeles son casi blancos
        private boolean filaClara(java.awt.image.BufferedImage img, int y) {
            int claros = 0;
            for (int x = 0; x < img.getWidth(); x++) {
                int rgb = img.getRGB(x, y);
                int brillo = (((rgb >> 16) & 255) + ((rgb >> 8) & 255) + (rgb & 255)) / 3;
                if (brillo > 200) {
                    claros++;
                }
            }
            return claros * 10 >= img.getWidth() * 4;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (imagen != null) {
                g.drawImage(imagen, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }

}