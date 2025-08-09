package laboratorio;

import javax.swing.*;
import java.awt.*;

public class SeleccionarLocalDialog extends JDialog {

    private final JComboBox<String> comboLocales = new JComboBox<>(new String[]{
        "Local_Surco", "Local_Miraflores", "Local_San_Juan", "Local_La_Molina"
    });

    private String localSeleccionado;

    public SeleccionarLocalDialog(Frame parent) {
        super(parent, "Seleccionar Local", true);
        setSize(300, 150);
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(null);

        JPanel center = new JPanel(new FlowLayout());
        center.add(new JLabel("Selecciona el local:"));
        center.add(comboLocales);
        add(center, BorderLayout.CENTER);

        JButton btnIniciar = new JButton("Iniciar");
        btnIniciar.addActionListener(e -> {
            localSeleccionado = (String) comboLocales.getSelectedItem();
            dispose();
        });
        add(btnIniciar, BorderLayout.SOUTH);

        setVisible(true);
    }

    public String getLocalSeleccionado() {
        return localSeleccionado;
    }
}
