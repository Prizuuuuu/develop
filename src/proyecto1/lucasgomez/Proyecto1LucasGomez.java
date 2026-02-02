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
                clases.Dashboard ventana = new clases.Dashboard();
                ventana.setVisible(true);
                
                // 2. Crear el Sistema Operativo (Le pasamos la ventana)
                // OJO: Si te marca error aquí, es porque falta importar la clase.
                clases.SistemaOperativo so = new clases.SistemaOperativo(ventana);
                
                // 3. Crear el Reloj (Le pasamos la ventana Y el sistema operativo)
                clases.Reloj reloj = new clases.Reloj(ventana, so);
                
                // 4. Configurar el Botón para Iniciar/Pausar
                ventana.getBtnIniciar().addActionListener(new java.awt.event.ActionListener() {
                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        if (ventana.getBtnIniciar().getText().equals("INICIAR SIMULACIÓN")) {
                            reloj.iniciar();
                            ventana.getBtnIniciar().setText("PAUSAR");
                        } else {
                            reloj.pausar();
                            ventana.getBtnIniciar().setText("INICIAR SIMULACIÓN");
                        }
                    }
                });
                
                // 5. Arrancar el hilo en segundo plano
                reloj.start();
            }
        });
    }
}