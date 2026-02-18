package clases;

import javax.swing.*;
import java.awt.*;

public class Dashboard extends JFrame {

    // --- Componentes Globales ---
    private JLabel lblReloj;
    private JLabel lblEstadoCPU;
    
    // --- Áreas de visualización ---
    private JTextArea txtColaListos;
    private JTextArea txtColaBloqueados;
    private JTextArea txtColaSuspendidos; // Listos-Suspendidos
    private JTextArea txtColaBloqSusp;    // Bloqueados-Suspendidos
    private JTextArea txtCPU;             // Proceso en ejecución
    
    // --- Botones de Control ---
    private JButton btnIniciar;
    private JButton btnInterrupcion;
    private JComboBox<String> cmbAlgoritmos;

    public Dashboard() {
        initComponents();
        configurarVentana();
    }

    private void configurarVentana() {
        setTitle("UNIMET-Sat RTOS: Monitor de Misión");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        setResizable(false);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(20, 25, 30)); 

        // === 1. HEADER: Reloj y Estado ===
        JPanel pnlHeader = new JPanel(new GridLayout(1, 3));
        pnlHeader.setOpaque(false);
        pnlHeader.setBorder(BorderFactory.createEmptyBorder(15, 15, 0, 15));
        
        JLabel lblTitulo = new JLabel("SATÉLITE ORBITAL: SISTEMA OPERATIVO");
        lblTitulo.setForeground(Color.CYAN);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        
        lblReloj = new JLabel("TIEMPO MISIÓN: 0 ciclos", SwingConstants.CENTER);
        lblReloj.setForeground(Color.WHITE);
        lblReloj.setFont(new Font("Monospaced", Font.BOLD, 22));
        
        lblEstadoCPU = new JLabel("ESTADO: IDLE", SwingConstants.RIGHT);
        lblEstadoCPU.setForeground(Color.GREEN);
        
        pnlHeader.add(lblTitulo);
        pnlHeader.add(lblReloj);
        pnlHeader.add(lblEstadoCPU);
        add(pnlHeader, BorderLayout.NORTH);

        // === 2. CENTER: Las Colas y la CPU ===
        JPanel pnlCentral = new JPanel(new GridLayout(2, 3, 15, 15)); 
        pnlCentral.setOpaque(false);
        pnlCentral.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // -- Fila Superior 
        txtColaListos = crearPanelCola(pnlCentral, "Cola de Listos (RAM)", Color.GREEN);
        txtCPU = crearPanelCola(pnlCentral, "CPU - Ejecución", Color.RED);
        txtColaBloqueados = crearPanelCola(pnlCentral, "Cola de Bloqueados (E/S)", Color.ORANGE);

        // -- Fila Inferior 
        txtColaSuspendidos = crearPanelCola(pnlCentral, "Listos-Suspendidos (Swap)", Color.GRAY);
        JTextArea txtLogs = crearPanelCola(pnlCentral, "Log del Sistema", Color.WHITE); 
        txtColaBloqSusp = crearPanelCola(pnlCentral, "Bloqueados-Suspendidos (Swap)", Color.GRAY);

        add(pnlCentral, BorderLayout.CENTER);

        // === 3. FOOTER: Controles ===
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        pnlFooter.setBackground(new Color(40, 45, 50));

        btnIniciar = new JButton("INICIAR SIMULACIÓN");
        btnIniciar.setFont(new Font("Arial", Font.BOLD, 14));
        
        btnInterrupcion = new JButton("⚠️ ALERTA: METEORITO");
        btnInterrupcion.setBackground(Color.RED);
        btnInterrupcion.setForeground(Color.WHITE);
        
        String[] algoritmos = {"FCFS", "Round Robin", "SRT", "Prioridad", "EDF"};
        cmbAlgoritmos = new JComboBox<>(algoritmos);

        pnlFooter.add(btnIniciar);
        pnlFooter.add(new JLabel("Politica:"));
        pnlFooter.add(cmbAlgoritmos);
        pnlFooter.add(btnInterrupcion);
        
        add(pnlFooter, BorderLayout.SOUTH);
    }

    // Método auxiliar para no repetir código creando paneles
    private JTextArea crearPanelCola(JPanel padre, String titulo, Color colorBorde) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(colorBorde, 2), 
                titulo, 
                0, 0, 
                new Font("Arial", Font.BOLD, 14), 
                Color.WHITE));
        
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setBackground(new Color(10, 10, 10));
        area.setForeground(Color.GREEN);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setText("\n  [Esperando procesos...]");
        
        panel.add(new JScrollPane(area), BorderLayout.CENTER);
        padre.add(panel);
        return area;
    }

    
    public JButton getBtnIniciar() { return btnIniciar; }
    public JTextArea getTxtColaListos() { return txtColaListos; }
    public JLabel getLblReloj() {
        return lblReloj;
    }

// --- MÉTODOS DE ACCESO (GETTERS) ---
    // Esto permite que otras clases toquen los componentes de la ventana

    public javax.swing.JTextArea getTxtCPU() {
        return txtCPU;
    }
    
    public javax.swing.JTextArea getTxtColaBloqueados(){
        return txtColaBloqueados;
    }
    
    public javax.swing.JButton getBtnInterrupcion() {
        return btnInterrupcion;
    }
}

