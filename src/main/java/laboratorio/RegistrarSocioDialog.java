package laboratorio;

import org.apache.xmlrpc.client.XmlRpcClient;

import javax.swing.*;
import java.awt.*;
import java.util.Vector;

public class RegistrarSocioDialog extends JDialog {

    private final JTextField txtId = new JTextField();
    private final JTextField txtNombre = new JTextField();
    private final JComboBox<String> comboMembresia = new JComboBox<>(new String[] {
        "Estándar", "Premium", "VIP"
    });

    private final XmlRpcClient rpcClient;

    public RegistrarSocioDialog(JFrame parent, XmlRpcClient rpcClient) {
        super(parent, "Registrar Socio", true);
        this.rpcClient = rpcClient;

        setSize(400, 200);
        setLayout(new GridLayout(4, 2, 10, 10));
        setLocationRelativeTo(parent);

        add(new JLabel("ID Socio:"));
        add(txtId);

        add(new JLabel("Nombre completo:"));
        add(txtNombre);

        add(new JLabel("Tipo de membresía:"));
        add(comboMembresia);

        JButton btnGuardar = new JButton("Registrar");
        btnGuardar.addActionListener(e -> registrarSocio());
        add(new JLabel());
        add(btnGuardar);

        setVisible(true);
    }

    private void registrarSocio() {
        try {
            int id = Integer.parseInt(txtId.getText().trim());
            String nombre = txtNombre.getText().trim();
            String tipo = (String) comboMembresia.getSelectedItem();

            if (nombre.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El nombre no puede estar vacío.");
                return;
            }

            Vector<Object> params = new Vector<>();
            params.add(id);
            params.add(nombre);
            params.add(tipo);

            boolean registrado = (Boolean) rpcClient.execute("registrarSocio", params);

            if (registrado) {
                JOptionPane.showMessageDialog(this, "✔ Socio registrado correctamente.");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "❌ Error: el socio ya existe o hubo un problema.");
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "El ID debe ser un número entero.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "❌ Error al registrar socio: " + ex.getMessage());
        }
    }
}
