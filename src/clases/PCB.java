package clases;

public class PCB {
    private static int contadorId = 1; // Para autogenerar IDs (1, 2, 3...)
    
    private int id;
    private String nombre;
    private int programCounter; // PC
    private int mar;            // Memory Address Register
    private String estado;      // New, Ready, Running, Blocked, Exit
    private int prioridad;      // 1 (Alta) a 3 (Baja) - Según PDF
    private int deadline;       // Ciclo límite para terminar
    private int instruccionesTotales;
    
    // Constructor para procesos aleatorios
    public PCB(String nombre, int instrucciones, int prioridad, int deadline) {
        this.id = contadorId++;
        this.nombre = nombre;
        this.instruccionesTotales = instrucciones;
        this.prioridad = prioridad;
        this.deadline = deadline;
        
        // Valores iniciales por defecto
        this.programCounter = 0;
        this.mar = 0;
        this.estado = "NEW";
    }

    // --- Método vital para ver el proceso en la pantalla ---
    // Esto es lo que se imprimirá en los cuadros verdes
    @Override
    public String toString() {
        return String.format("[ID:%03d] %-15s | PC:%02d | Pri:%d", id, nombre, programCounter, prioridad);
    }
    
    // Getters necesarios
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public int getPrioridad() { return prioridad; }
}