package laboratorio;

import org.apache.xmlrpc.client.XmlRpcClient;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Vector;
import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JDateChooser;
import java.util.Date;
import java.text.SimpleDateFormat;

public class EnviarCabeceraDialog extends JDialog {

    private final JComboBox<String> comboSocios = new JComboBox<>();
    private final JTextField txtFecha = new JTextField(10);

    private final XmlRpcClient rpcClient;
    private final String DB_URL = "jdbc:mysql://localhost:3306/utpbdlocal";
    private final String DB_USER = "root";
    private final String DB_PASS = "";

    private boolean enviado = false;
    private final JDateChooser dateChooser = new JDateChooser();
    private final String ID_LOCAL;
    
    public EnviarCabeceraDialog(JFrame parent, XmlRpcClient rpcClient, String idLocal){
        super(parent, "Enviar Cabecera", true);
        this.rpcClient = rpcClient;
        this.ID_LOCAL = idLocal;

        setSize(400, 200);
        setLayout(new GridLayout(4, 2, 10, 10));
        setLocationRelativeTo(parent);

        cargarSocios();

        
        add(new JLabel("Socio:"));
        add(comboSocios);

        add(new JLabel("Fecha de consumo:"));
        dateChooser.setDateFormatString("yyyy-MM-dd");
        add(dateChooser);

        JButton btnEnviar = new JButton("Enviar");
        btnEnviar.addActionListener(e -> enviarCabecera());

        add(new JLabel());
        add(btnEnviar);

        setVisible(true);
    }

    private void cargarSocios() {
        try {
            Object[] socios = (Object[]) rpcClient.execute("obtenerSocios", new Vector<>());
            for (Object obj : socios) {
                Object[] socio = (Object[]) obj;
                comboSocios.addItem(socio[0] + " - " + socio[1]); // ID - Nombre
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Error al cargar socios: " + e.getMessage());
        }
    }

    private void enviarCabecera() {
        String idSocio = obtenerIdSeleccionado(comboSocios);
        Date selectedDate = (Date) dateChooser.getDate();
        if (selectedDate == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar una fecha.");
            return;
        }
        String fecha = new java.text.SimpleDateFormat("yyyy-MM-dd").format(selectedDate);

        if (fecha.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Fecha obligatoria.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT SUM(subtotal) FROM consumos_detalle WHERE id_socio = ? AND fecha_consumo = ?"
            );
            ps.setInt(1, Integer.parseInt(idSocio));
            ps.setString(2, fecha);
            ResultSet rs = ps.executeQuery();

            double monto = 0;
            if (rs.next()) {
                monto = rs.getDouble(1);
            }

            if (monto <= 0) {
                JOptionPane.showMessageDialog(this, "⚠ No hay consumos registrados para esa fecha.");
                return;
            }

            Vector<Object> params = new Vector<>();
            params.add(ID_LOCAL);
            params.add(Integer.parseInt(idSocio));
            params.add(fecha);
            params.add(monto);

            boolean ok = (Boolean) rpcClient.execute("registrarCabeceraConsumo", params);
            if (ok) {
                JOptionPane.showMessageDialog(this, "✔ Cabecera enviada correctamente.");
                enviado = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "❌ Error al registrar la cabecera.");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Error: " + e.getMessage());
        }
    }

    private String obtenerIdSeleccionado(JComboBox<String> comboBox) {
        String seleccionado = (String) comboBox.getSelectedItem();
        if (seleccionado != null && seleccionado.contains(" - ")) {
            return seleccionado.split(" - ")[0].trim();
        }
        return seleccionado;
    }

    public boolean fueEnviado() {
        return enviado;
    }
}
