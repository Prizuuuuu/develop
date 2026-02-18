package clases;

public class SistemaOperativo {

    private Dashboard gui;
    private Cola<PCB> colaListos;
    private Cola<PCB> colaBloqueados;
    private PCB procesoEnCPU;
    
    public SistemaOperativo(Dashboard gui) {
        this.gui = gui;
        this.colaListos = new Cola<>();
        this.colaBloqueados = new Cola();
        this.procesoEnCPU = null;
        initSistema();
    }
    
    private void initSistema() {
        // Procesos de prueba
        colaListos.encolar(new PCB("System_Boot", 10, 1, 50));
        colaListos.encolar(new PCB("Antenna_Check", 5, 2, 60));
        colaListos.encolar(new PCB("Beacon_Signal", 20, 1, 100));
        if (gui != null) {
            gui.getBtnInterrupcion().addActionListener(e -> {
                bloquearProceso();
            });
        }
        actualizarGUI();
    }
    
    public void ejecutarCiclo() {
        // 1. Planificador (Scheduler): ¿El CPU está libre?
        if (procesoEnCPU == null) {
            if (!colaListos.esVacia()) {
                procesoEnCPU = colaListos.desencolar();
                procesoEnCPU.setEstado("RUNNING");
            }
        }
        
        // 2. Ejecutar proceso (CPU)
        if (procesoEnCPU != null) {
            procesoEnCPU.avanzarInstruccion();
            
            // Si termina, lo sacamos (Simulación básica de salida)
            // Por ahora, si llega a 10 instrucciones, lo matamos para probar
            if (procesoEnCPU.getInstruccionesTotales() <= 0) {
                 procesoEnCPU.setEstado("EXIT");
                 procesoEnCPU = null; // CPU queda libre para el siguiente
            }
        }
        
        // 3. Refrescar pantallas
        actualizarGUI();
    }
    
    public void bloquearProceso() {
        // Solo podemos bloquear si hay alguien en el CPU
        if (procesoEnCPU != null) {
            // 1. Cambiamos su estado a BLOQUEADO
            procesoEnCPU.setEstado("BLOCKED");
            
            // 2. Lo mandamos a la cola amarilla
            colaBloqueados.encolar(procesoEnCPU);
            
            // 3. Liberamos el CPU
            procesoEnCPU = null;
            
            // 4. Actualizamos la pantalla para ver el cambio inmediato
            actualizarGUI();
        }
    }
    
    private void actualizarGUI() {
        // Panel Verde (Cola de Listos)
        if (gui != null) {
            gui.getTxtColaListos().setText(colaListos.toString());
            
            // Panel Rojo (CPU)
            if (procesoEnCPU != null) {
                gui.getTxtCPU().setText(procesoEnCPU.toString());
            } else {
                gui.getTxtCPU().setText("[Esperando procesos...]");
            }
            if (colaBloqueados.esVacia()) {
                gui.getTxtColaBloqueados().setText("[Cola vacía]");
            } else {
                gui.getTxtColaBloqueados().setText(colaBloqueados.toString());
            }
        }
    }
}