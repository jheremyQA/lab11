package laboratorio;

import org.apache.xmlrpc.client.XmlRpcClient;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Vector;
import com.toedter.calendar.JDateChooser;
import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;
import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;
import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;


public class RegistrarConsumoDialog extends JDialog {

    private final JComboBox<String> comboSocios = new JComboBox<>();
    private final JComboBox<String> comboProductos = new JComboBox<>();
    private final JTextField txtCantidad = new JTextField(10);
    private final JLabel lblPrecioUnitario = new JLabel("0.00");
    private final XmlRpcClient rpcClient;
    private boolean registrado = false;

    private final String DB_URL = "jdbc:mysql://localhost:3306/utpbdlocal";
    private final String DB_USER = "root";
    private final String DB_PASS = "";

    private final JDateChooser dateChooser = new JDateChooser();
    

    public RegistrarConsumoDialog(JFrame parent, XmlRpcClient rpcClient) {
        super(parent, "Registrar Consumo", true);
        this.rpcClient = rpcClient;

        setSize(400, 300);
        setLayout(new GridLayout(6, 2, 5, 5));
        setLocationRelativeTo(parent);

        cargarSocios();
        cargarProductos();

        add(new JLabel("Socio:"));
        add(comboSocios);

        add(new JLabel("Producto:"));
        add(comboProductos);

        add(new JLabel("Precio Unitario:"));
        add(lblPrecioUnitario);

        add(new JLabel("Cantidad:"));
        add(txtCantidad);

        dateChooser.setDateFormatString("yyyy-MM-dd");
        add(new JLabel("Fecha:"));
        add(dateChooser);

        JButton btnRegistrar = new JButton("Registrar");
        btnRegistrar.addActionListener(e -> registrarConsumo());

        add(new JLabel());
        add(btnRegistrar);

        comboProductos.addActionListener(e -> actualizarPrecio());

        setVisible(true);
    }

    private void cargarSocios() {
        try {
            Vector<Object> params = new Vector<>();
            Object[] socios = (Object[]) rpcClient.execute("obtenerSocios", params);
            for (Object obj : socios) {
                Object[] s = (Object[]) obj;
                comboSocios.addItem(s[0] + " - " + s[1]); // ID - Nombre
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar socios: " + e.getMessage());
        }
    }

    private void cargarProductos() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id_producto_servicio, descripcion FROM productos_servicios");
            while (rs.next()) {
                String id = rs.getString(1);
                String desc = rs.getString(2);
                comboProductos.addItem(id + " - " + desc);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar productos: " + e.getMessage());
        }
    }

    private String obtenerIdSeleccionado(JComboBox<String> comboBox) {
        String seleccionado = (String) comboBox.getSelectedItem();
        if (seleccionado != null && seleccionado.contains(" - ")) {
            return seleccionado.split(" - ")[0].trim();
        }
        return seleccionado;
    }

    private String obtenerDescripcion(String idProd) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            PreparedStatement ps = conn.prepareStatement("SELECT descripcion FROM productos_servicios WHERE id_producto_servicio = ?");
            ps.setString(1, idProd);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("descripcion");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al obtener descripción: " + e.getMessage());
        }
        return "";
    }

    private void actualizarPrecio() {
        String idProducto = obtenerIdSeleccionado(comboProductos);
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            PreparedStatement ps = conn.prepareStatement("SELECT precio_unitario FROM productos_servicios WHERE id_producto_servicio = ?");
            ps.setString(1, idProducto);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                double precio = rs.getDouble(1);
                lblPrecioUnitario.setText(String.format("%.2f", precio));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al obtener precio: " + e.getMessage());
        }
    }

    private void registrarConsumo() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String idSocio = obtenerIdSeleccionado(comboSocios);
            String idProd = obtenerIdSeleccionado(comboProductos);

            // Validaciones simples
            if (txtCantidad.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Debe ingresar una cantidad.");
                return;
            }

            double precio = Double.parseDouble(lblPrecioUnitario.getText());
            int cantidad = Integer.parseInt(txtCantidad.getText());
            double subtotal = cantidad * precio;

            Date selectedDate = dateChooser.getDate();
            if (selectedDate == null) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar una fecha.");
                return;
            }
            String fecha = new SimpleDateFormat("yyyy-MM-dd").format(selectedDate);
            if (fecha == null || fecha.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Fecha obligatoria.");
                return;
            }

            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO consumos_detalle (id_socio, fecha_consumo, id_producto_servicio, descripcion, cantidad, precio_unitario, subtotal) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)"
            );
            ps.setInt(1, Integer.parseInt(idSocio));
            ps.setString(2, fecha);
            ps.setString(3, idProd);
            ps.setString(4, obtenerDescripcion(idProd));
            ps.setInt(5, cantidad);
            ps.setDouble(6, precio);
            ps.setDouble(7, subtotal);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "✔ Consumo registrado correctamente.");
            registrado = true;
            dispose(); // Cierra el popup

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Error al registrar: " + e.getMessage());
        }
    }

    public boolean fueRegistrado() {
        return registrado;
    }

}
