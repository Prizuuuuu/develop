package proyecto1.lucasgomez;

import clases.Dashboard;
import clases.Reloj;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Proyecto1LucasGomez {

    public static void main(String[] args) {
        // Ejecutar la GUI
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                // 1. Crear la Ventana
                Dashboard ventana = new Dashboard();
                ventana.setVisible(true);
                
                // 2. Crear el Reloj y darle la referencia de la ventana
                Reloj reloj = new Reloj(ventana);
                
                // 3. Arrancar el hilo del reloj (se quedará esperando en "pausa")
                reloj.start();
                
                // 4. Conectar el botón "INICIAR SIMULACIÓN"
                ventana.getBtnIniciar().addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (ventana.getBtnIniciar().getText().equals("INICIAR SIMULACIÓN")) {
                            reloj.iniciar();
                            ventana.getBtnIniciar().setText("PAUSAR");
                        } else {
                            reloj.pausar();
                            ventana.getBtnIniciar().setText("INICIAR SIMULACIÓN");
                        }
                    }
                });
            }
        });
    }
}