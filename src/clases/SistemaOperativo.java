package clases;

public class SistemaOperativo {

    private Dashboard gui;
    private Cola<PCB> colaListos;
    private Cola<PCB> colaBloqueados;
    private Cola<PCB> colaListosSuspendidos;
    private Cola<PCB> colaBloqueadosSuspendidos;
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
        this.colaListosSuspendidos = new Cola<>();
    this.colaBloqueadosSuspendidos = new Cola<>();
        if (gui != null) {
            gui.getBtnInterrupcion().addActionListener(e -> {
                bloquearProceso();
                gui.imprimirLog("!! ALERTA: Interrupción de Hardware (Meteorito)");
            });
        }
        if(gui != null) gui.imprimirLog(">> Sistema Iniciado: Procesos cargados en RAM.");
        actualizarGUI();
    }
    
    public void ejecutarCiclo() {
        
        // -------------------------------------------------------------
        // 1. GESTOR DE TRÁFICO (BLOQUEADOS Y SWAP) - ¡AL PRINCIPIO!
        // -------------------------------------------------------------
        
        // A. Revisar Bloqueados (Amarillo) -> Mover a RAM o Disco
        if (!colaBloqueados.esVacia()) {
            // Aumentamos probabilidad al 50% para que lo veas moverse YA
            if (Math.random() < 0.5) { 
                PCB p = colaBloqueados.desencolar();
                
                // 50% de probabilidad: Vuelve a RAM (Verde)
                if (Math.random() < 0.5) {
                    p.setEstado("READY");
                    colaListos.encolar(p);
                    gui.imprimirLog("✅ [I/O] Fin de espera. Vuelve a Listos: " + p.getNombre());
                } else {
                    // 50% de probabilidad: Se va a DISCO (Gris Abajo-Der)
                    colaBloqueadosSuspendidos.encolar(p);
                    gui.imprimirLog("⬇ [SWAP] Llevando a Disco: " + p.getNombre());
                }
            }
        }

        // B. Disco Duro: De Bloq-Susp a Listo-Susp (Derecha a Izquierda abajo)
        if (!colaBloqueadosSuspendidos.esVacia() && Math.random() < 0.2) { 
            PCB p = colaBloqueadosSuspendidos.desencolar();
            colaListosSuspendidos.encolar(p);
            gui.imprimirLog("💾 [DISCO] Transferencia interna completada: " + p.getNombre());
        }

        // C. Swap-In: De Disco a RAM (Subir al Verde)
        if (!colaListosSuspendidos.esVacia() && Math.random() < 0.2) {
            PCB p = colaListosSuspendidos.desencolar();
            p.setEstado("READY");
            colaListos.encolar(p);
            gui.imprimirLog("⬆ [SWAP] ¡Recuperado a RAM!: " + p.getNombre());
        }

        // -------------------------------------------------------------
        // 2. PLANIFICADOR DE CPU (LO QUE YA TENÍAS)
        // -------------------------------------------------------------
        
        // Si el CPU está libre, busca trabajo
        if (procesoEnCPU == null) {
            if (!colaListos.esVacia()) {
                procesoEnCPU = colaListos.desencolar();
                procesoEnCPU.setEstado("RUNNING");
                gui.imprimirLog("[CPU] Ejecutando: " + procesoEnCPU.getNombre());
            } else {
                // Si no hay nada en RAM, el CPU descansa
                gui.getTxtCPU().setText("[Esperando procesos...]"); 
            }
        }

        // Si hay un proceso, lo ejecuta
        if (procesoEnCPU != null) {
            procesoEnCPU.ejecutar(); // Resta 1 instrucción
            
            // Si terminó
            if (procesoEnCPU.getInstruccionesRestantes() <= 0) {
                procesoEnCPU.setEstado("TERMINATED");
                gui.imprimirLog("🏁 [FIN] Proceso terminado: " + procesoEnCPU.getNombre());
                procesoEnCPU = null; // Liberar CPU
            }
        }
        
        // Actualizar pantalla siempre al final
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
            // Paneles de Swap
        if (gui != null) {
            gui.getTxtColaSuspendidos().setText(colaListosSuspendidos.toString());
            gui.getTxtColaBloqSusp().setText(colaBloqueadosSuspendidos.toString());
        }
        }
    }
}