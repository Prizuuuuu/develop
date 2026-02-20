package clases;

public class SistemaOperativo {

    private Dashboard gui;
    private Cola<PCB> colaListos;
    private Cola<PCB> colaBloqueados;
    private Cola<PCB> colaListosSuspendidos;
    private Cola<PCB> colaBloqueadosSuspendidos;
    private PCB procesoEnCPU;
    
    // --- VARIABLES DEL RELOJ Y CONTROL (HILOS) ---
    private int relojGlobal = 0;          // Cuenta los ciclos totales
    private int duracionCiclo = 2000;     // 1000 ms = 1 segundo por defecto
    private boolean simulacionActiva = false; // Controla si el hilo corre o se detiene
    private Thread hiloReloj; 
    
    // --- VARIABLES DE MEMORIA ---
    private final int LIMITE_MEMORIA = 5; // Máximo 5 procesos permitidos en RAM a la vez
    
    // --- VARIABLES DE PLANIFICACIÓN (ALGORITMOS) ---
    private String algoritmoActual = "FCFS"; // Algoritmo por defecto
    private int quantumRR = 3;               // RR: 3 ciclos de tiempo en CPU
    private int contadorQuantum = 0;         // RR: Cuenta cuánto lleva el proceso actual
    
    public SistemaOperativo(Dashboard gui) {
        this.gui = gui;
        this.colaListos = new Cola<>();
        this.colaBloqueados = new Cola<>();
        this.procesoEnCPU = null;
        initSistema();
    }
    
    public void admitirProceso(PCB p) {
        int procesosEnRAM = colaListos.getTamano() + colaBloqueados.getTamano();
        if (procesoEnCPU != null) procesosEnRAM++; // Sumamos el que está en CPU

        if (procesosEnRAM < LIMITE_MEMORIA) {
            // Hay espacio: Va a RAM (Verde)
            p.setEstado("READY");
            colaListos.encolar(p);
            if(gui != null) gui.imprimirLog("🟢 [RAM] Nuevo proceso en RAM: " + p.getNombre());
        } else {
            // No hay espacio: Va a Disco (Swap - Listo Suspendido)
            p.setEstado("READY_SUSPENDED");
            colaListosSuspendidos.encolar(p);
            if(gui != null) gui.imprimirLog("💾 [DISCO] Memoria Llena. Cae en Suspendido: " + p.getNombre());
        }
        actualizarGUI();
    }
    
    private void initSistema() {
        // Inicializar colas de Swap que faltaban arriba
        this.colaListosSuspendidos = new Cola<>();
        this.colaBloqueadosSuspendidos = new Cola<>();

        // --- LOS 10 PROCESOS INICIALES ---
        admitirProceso(new PCB("System_Boot", 6, 1, 50));
        admitirProceso(new PCB("Antenna_Check", 5, 2, 60));
        admitirProceso(new PCB("Beacon_Signal", 8, 1, 80));
        admitirProceso(new PCB("Solar_Panels", 10, 3, 90));
        admitirProceso(new PCB("Camera_Init", 7, 2, 70));
        admitirProceso(new PCB("Temp_Sensors", 4, 1, 40));
        admitirProceso(new PCB("Data_Compress", 12, 4, 110));
        admitirProceso(new PCB("Telemetry_Tx", 9, 1, 85));
        admitirProceso(new PCB("Battery_Heater", 6, 2, 65));
        admitirProceso(new PCB("Gyroscope_Cal", 11, 3, 95));
        
        if (gui != null) {
            // Acción del botón Meteorito
            gui.getBtnInterrupcion().addActionListener(e -> {
                bloquearProceso();
                gui.imprimirLog("!! ALERTA: Interrupción de Hardware (Meteorito)");
            });
            
            // Acción del botón Iniciar / Pausar
            gui.getBtnIniciar().addActionListener(e -> {
                if (!simulacionActiva) {
                    iniciarSimulacion(); 
                    gui.getBtnIniciar().setText("PAUSAR SIMULACIÓN"); 
                } else {
                    detenerSimulacion();
                    gui.getBtnIniciar().setText("REANUDAR SIMULACIÓN"); 
                }
            });
            
            // Acción para el ComboBox de algoritmos
            if (gui.getCmbAlgoritmos() != null) {
                gui.getCmbAlgoritmos().addActionListener(e -> {
                    algoritmoActual = gui.getCmbAlgoritmos().getSelectedItem().toString();
                    gui.imprimirLog("🔄 ALGORITMO CAMBIADO A: " + algoritmoActual);
                    contadorQuantum = 0; // Reiniciamos el reloj de RR por si acaso
                });
            }    
        }
        
        if(gui != null) gui.imprimirLog(">> Sistema Iniciado: 10 Procesos cargados.");
        actualizarGUI();
    }
    
    // --- MÉTODOS DEL HILO (THREAD) ---

    public void iniciarSimulacion() {
        if (!simulacionActiva) { 
            simulacionActiva = true;
            
            hiloReloj = new Thread(() -> {
                while (simulacionActiva) {
                    try {
                        relojGlobal++; 
                        
                        if (gui != null) {
                            gui.imprimirLog("=====================================");
                            gui.imprimirLog("⏱️ INICIANDO CICLO DE RELOJ: " + relojGlobal);
                        }
                        
                        ejecutarCiclo(); 
                        
                        Thread.sleep(duracionCiclo); 
                        
                    } catch (InterruptedException e) {
                        if(gui != null) gui.imprimirLog("⚠️ Error en el hilo del reloj: " + e.getMessage());
                    }
                }
            });
            
            hiloReloj.start(); 
            if(gui != null) gui.imprimirLog("🚀 SIMULACIÓN INICIADA");
        }
    }

    public void detenerSimulacion() {
        simulacionActiva = false;
        if (gui != null) {
            gui.imprimirLog("🛑 SIMULACIÓN DETENIDA");
        }
    }

    public void cambiarVelocidad(int milisegundos) {
        this.duracionCiclo = milisegundos;
        if (gui != null) {
            gui.imprimirLog("⚙️ Velocidad cambiada a " + milisegundos + " ms por ciclo.");
        }
    }
    
    public int getRelojGlobal() {
        return relojGlobal;
    }
    
    // Método especial para EDF CORREGIDO
    private PCB sacarProcesoMenorDeadline() {
        if (colaListos.esVacia()) return null;
        
        int tamano = colaListos.getTamano(); // <-- MEDIMOS ANTES DE SACAR
        PCB mejorProceso = colaListos.desencolar();
        int menorDeadline = mejorProceso.getDeadline();
        colaListos.encolar(mejorProceso); 
        
        for (int i = 1; i < tamano; i++) {
            PCB p = colaListos.desencolar();
            if (p.getDeadline() < menorDeadline) {
                menorDeadline = p.getDeadline();
                mejorProceso = p;
            }
            colaListos.encolar(p);
        }
        
        for (int i = 0; i < tamano; i++) {
            PCB p = colaListos.desencolar();
            if (p != mejorProceso) {
                colaListos.encolar(p); 
            }
        }
        
        return mejorProceso;
    }
    
    // MÉTODO NUEVO PARA PRIORIDAD CORREGIDO
    private PCB sacarProcesoMayorPrioridad() {
        if (colaListos.esVacia()) return null;
        
        int tamano = colaListos.getTamano(); // <-- MEDIMOS ANTES DE SACAR
        PCB mejorProceso = colaListos.desencolar();
        int mejorPrioridad = mejorProceso.getPrioridad();
        colaListos.encolar(mejorProceso); 
        
        for (int i = 1; i < tamano; i++) {
            PCB p = colaListos.desencolar();
            if (p.getPrioridad() < mejorPrioridad) { 
                mejorPrioridad = p.getPrioridad();
                mejorProceso = p;
            }
            colaListos.encolar(p);
        }
        
        for (int i = 0; i < tamano; i++) {
            PCB p = colaListos.desencolar();
            if (p != mejorProceso) {
                colaListos.encolar(p); 
            }
        }
        
        return mejorProceso;
    }
    
    public void ejecutarCiclo() {
        
        // -------------------------------------------------------------
        // 1. GESTOR DE TRÁFICO (BLOQUEADOS Y SWAP) 
        // -------------------------------------------------------------
        
        int procesosEnRAM = colaListos.getTamano() + colaBloqueados.getTamano() + (procesoEnCPU != null ? 1 : 0);

        if (procesosEnRAM < LIMITE_MEMORIA && !colaListosSuspendidos.esVacia()) {
            PCB p = colaListosSuspendidos.desencolar();
            p.setEstado("READY");
            colaListos.encolar(p);
            procesosEnRAM++; 
            if (gui != null) gui.imprimirLog("⬆ [SWAP-IN] Recuperado a RAM: " + p.getNombre());
        }

        if (!colaBloqueados.esVacia() && Math.random() < 0.2) { 
            PCB p = colaBloqueados.desencolar();
            p.setEstado("READY");
            colaListos.encolar(p);
            if (gui != null) gui.imprimirLog("✅ [I/O] Fin de espera. Vuelve a Listos: " + p.getNombre());
        }

        // -------------------------------------------------------------
        // 2. PLANIFICADOR DE CPU (CON ALGORITMOS)
        // -------------------------------------------------------------
        
        if (algoritmoActual.contains("RR") || algoritmoActual.contains("Round")) {
            if (procesoEnCPU != null) {
                contadorQuantum++;
                if (contadorQuantum >= quantumRR && procesoEnCPU.getInstruccionesRestantes() > 0) {
                    procesoEnCPU.setEstado("READY");
                    colaListos.encolar(procesoEnCPU);
                    if(gui != null) gui.imprimirLog("⏱️ [RR] Fin de Quantum (3 ciclos). Vuelve a cola: " + procesoEnCPU.getNombre());
                    procesoEnCPU = null; 
                    contadorQuantum = 0; 
                }
            }
        }

        // --- B. ASIGNAR NUEVO PROCESO AL CPU ---
        if (procesoEnCPU == null) {
            if (!colaListos.esVacia()) {
                
                // ¡AQUÍ ESTÁ LA MAGIA DE LOS ALGORITMOS!
                if (algoritmoActual.contains("EDF")) {
                    procesoEnCPU = sacarProcesoMenorDeadline(); // Busca el más urgente
                } else if (algoritmoActual.contains("Prioridad") || algoritmoActual.contains("Priority")) {
                    procesoEnCPU = sacarProcesoMayorPrioridad(); // Busca el más importante
                } else {
                    procesoEnCPU = colaListos.desencolar(); // FCFS y RR toman el primero en la fila
                }
                
                procesoEnCPU.setEstado("RUNNING");
                contadorQuantum = 0; 
                if(gui != null) gui.imprimirLog("⚙️ [" + algoritmoActual + "] CPU Ejecutando: " + procesoEnCPU.getNombre());
                
            } else {
                if (gui != null) gui.getTxtCPU().setText("[Esperando procesos...]"); 
            }
        }

        // --- C. EJECUCIÓN Y VALIDACIÓN DE LÍMITES (DEADLINE) ---
        if (procesoEnCPU != null) {
            procesoEnCPU.ejecutar();  
            procesoEnCPU.envejecer(); 
            
            if (procesoEnCPU.getInstruccionesRestantes() <= 0) {
                procesoEnCPU.setEstado("TERMINATED");
                if(gui != null) gui.imprimirLog("🏁 [FIN] Proceso terminado con éxito: " + procesoEnCPU.getNombre());
                procesoEnCPU = null; 
            } else if (procesoEnCPU.getDeadline() <= 0) {
                procesoEnCPU.setEstado("FAILED");
                if(gui != null) gui.imprimirLog("❌ [FALLO DE MISIÓN] Deadline rebasado. Proceso abortado: " + procesoEnCPU.getNombre());
                procesoEnCPU = null;
            }
        }
        
        // -------------------------------------------------------------
        // 3. GENERADOR DINÁMICO DE PROCESOS (PASO 5)
        // -------------------------------------------------------------
        if (Math.random() < 0.10) {
            int inst = (int) (Math.random() * 10) + 5;     
            int prio = (int) (Math.random() * 5) + 1;      
            int dead = (int) (Math.random() * 50) + 30;    
            
            PCB nuevaTarea = new PCB("Task_" + relojGlobal, inst, prio, dead);
            if (gui != null) gui.imprimirLog("📡 [NUEVA TAREA] Señal recibida desde la Tierra...");
            
            admitirProceso(nuevaTarea);
        }
        
        actualizarGUI();
    }
    
    public void bloquearProceso() {
        if (procesoEnCPU != null) {
            procesoEnCPU.setEstado("BLOCKED");
            colaBloqueados.encolar(procesoEnCPU);
            procesoEnCPU = null;
            actualizarGUI();
        }
    }
    
    private void actualizarGUI() {
        if (gui != null) {
            gui.getTxtColaListos().setText(colaListos.toString());
            
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
            gui.getTxtColaSuspendidos().setText(colaListosSuspendidos.toString());
            gui.getTxtColaBloqSusp().setText(colaBloqueadosSuspendidos.toString());
        }
    }
}