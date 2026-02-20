public class PCB {
    
    // --- VARIABLES GLOBALES PARA AUTOGENERAR ID ---
    private static int contadorGlobal = 1;

    private String id;
    private String nombre;
    private String status;
    private int pc;          
    private int mar;          
    private int prioridad;
    private int deadline;
    
    private int instruccionesTotales; 

    // --- CONSTRUCTOR ---
    public PCB(String nombre, int instruccionesTotales, int prioridad, int deadline) {
        this.id = "P" + String.format("%03d", contadorGlobal++); 
        this.nombre = nombre;
        this.instruccionesTotales = instruccionesTotales;
        this.prioridad = prioridad;
        this.deadline = deadline;
        
        this.status = "NEW"; 
        this.pc = 0;
        this.mar = 0;
    }

    public void ejecutar() {
        if (this.instruccionesTotales > 0) {
            this.instruccionesTotales--; // Resta la instrucción
            this.pc++;                   // Sube el Program Counter 
            this.mar++;                  // Sube el MAR 
        }
    }

    public void envejecer() {
        if (this.deadline > 0) {
            this.deadline--;
        }
    }

    // --- GETTERS Y SETTERS ---
    public String getId() { return id; }
    public String getNombre() { return nombre; }
    
    public String getStatus() { return status; }
    public void setEstado(String status) { this.status = status; } 
    
    public int getPc() { return pc; }
    public int getMar() { return mar; }
    public int getPrioridad() { return prioridad; }
    public int getDeadline() { return deadline; }
    public int getInstruccionesRestantes() { return instruccionesTotales; }

    @Override
    public String toString() {
        return "[" + id + "] " + nombre + " | Pri: " + prioridad + " | DL: " + deadline;
    }
}