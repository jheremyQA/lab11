package laboratorio;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.sql.*;
import java.util.Vector;
import java.awt.event.ActionListener;

public class ClienteGUI extends JFrame {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/utpbdlocal";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";
    private final String ID_LOCAL;

    private JTextArea outputArea;
    private XmlRpcClient rpcClient;

    public ClienteGUI(String idLocal) {
        this.ID_LOCAL = idLocal;
        configurarVentana();
        inicializarUI();
        inicializarRPC();
    }

    private void configurarVentana() {
        setTitle("Cliente UTP Club - GUI");
        setSize(700, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);
    }

    private void inicializarUI() {
        outputArea = new JTextArea();
        JScrollPane scroll = new JScrollPane(outputArea);
        add(scroll, BorderLayout.CENTER);

        JPanel botones = new JPanel(new GridLayout(2, 2));

        JButton registrarSocioBtn = new JButton("Registrar Socio");
        registrarSocioBtn.addActionListener(e -> new RegistrarSocioDialog(this, rpcClient));
        botones.add(registrarSocioBtn);

        agregarBoton(botones, "Registrar Consumo", this::registrarConsumo);
        agregarBoton(botones, "Enviar Cabecera al Servidor", this::enviarCabecera);
        agregarBoton(botones, "Consultar Socios", this::consultarSocios);
        agregarBoton(botones, "Consultar Cabeceras", this::consultarCabeceras);

        add(botones, BorderLayout.SOUTH);
    }

    private void agregarBoton(JPanel panel, String texto, ActionListener listener) {
        JButton boton = new JButton(texto);
        boton.addActionListener(listener);
        panel.add(boton);
    }

    private void inicializarRPC() {
        try {
            XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
            config.setServerURL(new URL("http://161.132.51.124:8000/RPC2"));
            rpcClient = new XmlRpcClient();
            rpcClient.setConfig(config);
            log("✅ Conectado a servidor RPC.");
        } catch (Exception e) {
            log("❌ Error conectando RPC: " + e.getMessage());
        }
    }

    private void registrarConsumo(ActionEvent e) {
        try {
            RegistrarConsumoDialog dialog = new RegistrarConsumoDialog(this, rpcClient);
            if (dialog.fueRegistrado()) {
                log("✔ Consumo registrado localmente.");
            } else {
                log("ℹ️ Registro cancelado o fallido.");
            }
        } catch (Exception ex) {
            log("❌ Error al abrir el formulario: " + ex.getMessage());
        }
    }

    private void enviarCabecera(ActionEvent e) {
        try {
            EnviarCabeceraDialog dialog = new EnviarCabeceraDialog(this, rpcClient, ID_LOCAL);
            if (dialog.fueEnviado()) {
                log("✔ Cabecera enviada al servidor.");
            } else {
                log("ℹ️ No se envió la cabecera.");
            }
        } catch (Exception ex) {
            log("❌ Error al abrir diálogo: " + ex.getMessage());
        }
    }

    private void consultarSocios(ActionEvent e) {
        try {
            Object[] resultado = (Object[]) rpcClient.execute("obtenerSocios", new Vector<>());
            log("📋 Socios registrados:");
            for (Object obj : resultado) {
                Object[] socio = (Object[]) obj;
                log("- ID: " + socio[0] + " | Nombre: " + socio[1] + " | Membresía: " + socio[2]);
            }
        } catch (Exception ex) {
            log("❌ Error al consultar socios: " + ex.getMessage());
        }
    }

    private void consultarCabeceras(ActionEvent e) {
        try {
            Vector<Object> params = new Vector<>();
            params.add(ID_LOCAL);

            Object[] resultado = (Object[]) rpcClient.execute("obtenerCabecerasConsumoPorLocal", params);
            log("📋 Cabeceras para " + ID_LOCAL + ":");
            for (Object obj : resultado) {
                Object[] cab = (Object[]) obj;
                log("- ID: " + cab[0] + " | Socio: " + cab[2] + " | Fecha: " + cab[3] + " | Monto: " + cab[4]);
            }
        } catch (Exception ex) {
            log("❌ Error al consultar cabeceras: " + ex.getMessage());
        }
    }

    private String prompt(String mensaje) {
        return JOptionPane.showInputDialog(this, mensaje);
    }

    private void log(String msg) {
        outputArea.append(msg + "\n");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SeleccionarLocalDialog selector = new SeleccionarLocalDialog(null);
            String local = selector.getLocalSeleccionado();

            if (local != null) {
                ClienteGUI app = new ClienteGUI(local);
                app.setVisible(true);
            } else {
                System.exit(0); // Si se cierra sin elegir
            }
        });
    }

}
