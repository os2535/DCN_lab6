import java.util.*;


public class TCPStateMachine {
    
    // TCP State definitions
    private enum State {
        CLOSED,
        LISTEN,
        SYN_SENT,
        SYN_RCVD,
        ESTABLISHED,
        FIN_WAIT_1,
        FIN_WAIT_2,
        CLOSING,
        TIME_WAIT,
        CLOSE_WAIT,
        LAST_ACK
    }
    
    // Current state of the FSM
    private State currentState;
    
    // Counter for SDATA/RDATA events
    private int dataCount;
    
    
    /* Constructor - initializes FSM to CLOSED state
     */
    public TCPStateMachine() {
        this.currentState = State.CLOSED;
        this.dataCount = 0;
    }
    
    /* Get the current state name
     */
    public String getCurrentStateName() {
        return currentState.toString();
    }
    
   
    public void processEvent(String event) {
        State previousState = currentState;
        
        switch (currentState) {
            case CLOSED:
                processClosed(event);
                break;
            case LISTEN:
                processListen(event);
                break;
            case SYN_SENT:
                processSynSent(event);
                break;
            case SYN_RCVD:
                processSynRcvd(event);
                break;
            case ESTABLISHED:
                processEstablished(event);
                break;
            case FIN_WAIT_1:
                processFinWait1(event);
                break;
            case FIN_WAIT_2:
                processFinWait2(event);
                break;
            case CLOSING:
                processClosing(event);
                break;
            case TIME_WAIT:
                processTimeWait(event);
                break;
            case CLOSE_WAIT:
                processCloseWait(event);
                break;
            case LAST_ACK:
                processLastAck(event);
                break;
        }
        
        // Display state transition if state changed
        if (previousState != currentState) {
            System.out.println("State transition: " + previousState + " -> " + currentState);
        }
    }
    
    
     /* Process events in CLOSED state
     */
    private void processClosed(String event) {
        switch (event) {
            case "PASSIVE":
                currentState = State.LISTEN;
                break;
            case "ACTIVE":
                currentState = State.SYN_SENT;
                break;
            default:
                handleInvalidEvent(event);
        }
    }
    
    /**
     * Process events in LISTEN state
     */
    private void processListen(String event) {
        switch (event) {
            case "SYN":
                currentState = State.SYN_RCVD;
                break;
            case "CLOSE":
                currentState = State.CLOSED;
                break;
            case "SEND":
                // Ignore SEND event in LISTEN state as per instructions
                System.out.println("SEND event ignored in LISTEN state");
                break;
            default:
                handleInvalidEvent(event);
        }
    }
    
    
     //Process events in SYN_SENT state
     
    private void processSynSent(String event) {
        switch (event) {
            case "SYN":
                currentState = State.SYN_RCVD;
                break;
            case "SYNACK":
                currentState = State.ESTABLISHED;
                break;
            case "CLOSE":
                currentState = State.CLOSED;
                break;
            default:
                handleInvalidEvent(event);
        }
    }
    
    
     // Process events in SYN_RCVD state
     
    private void processSynRcvd(String event) {
        switch (event) {
            case "ACK":
                currentState = State.ESTABLISHED;
                break;
            case "CLOSE":
                currentState = State.FIN_WAIT_1;
                break;
            default:
                handleInvalidEvent(event);
        }
    }
    
    /* Process events in ESTABLISHED state
       Special handling for RDATA and SDATA events
     */
    private void processEstablished(String event) {
        switch (event) {
            case "RDATA":
                dataCount++;
                System.out.println("DATA received " + dataCount);
                // Stay in ESTABLISHED state
                break;
            case "SDATA":
                dataCount++;
                System.out.println("DATA sent " + dataCount);
                // Stay in ESTABLISHED state
                break;
            case "FIN":
                currentState = State.CLOSE_WAIT;
                break;
            case "CLOSE":
                currentState = State.FIN_WAIT_1;
                break;
            default:
                handleInvalidEvent(event);
        }
    }
    
    /* Process events in FIN_WAIT_1 state
     */
    private void processFinWait1(String event) {
        switch (event) {
            case "FIN":
                currentState = State.CLOSING;
                break;
            case "ACK":
                currentState = State.FIN_WAIT_2;
                break;
            default:
                handleInvalidEvent(event);
        }
    }
    
    /*Process events in FIN_WAIT_2 state
     */
    private void processFinWait2(String event) {
        switch (event) {
            case "FIN":
                currentState = State.TIME_WAIT;
                break;
            default:
                handleInvalidEvent(event);
        }
    }
    
    /*Process events in CLOSING state
     */
    private void processClosing(String event) {
        switch (event) {
            case "ACK":
                currentState = State.TIME_WAIT;
                break;
            default:
                handleInvalidEvent(event);
        }
    }
    
    /*Process events in TIME_WAIT state
     */
    private void processTimeWait(String event) {
        switch (event) {
            case "TIMEOUT":
                currentState = State.CLOSED;
                break;
            default:
                handleInvalidEvent(event);
        }
    }
    
    /*Process events in CLOSE_WAIT state
     */
    private void processCloseWait(String event) {
        switch (event) {
            case "CLOSE":
                currentState = State.LAST_ACK;
                break;
            default:
                handleInvalidEvent(event);
        }
    }
    
    /**
     * Process events in LAST_ACK state
     */
    private void processLastAck(String event) {
        switch (event) {
            case "ACK":
                currentState = State.CLOSED;
                break;
            default:
                handleInvalidEvent(event);
        }
    }
    
    /* Handle invalid events
     */
    private void handleInvalidEvent(String event) {
        System.out.println("Error: unexpected Event: " + event);
    }
    
    /* Main program loop
     */
    public static void main(String[] args) {
        TCPStateMachine fsm = new TCPStateMachine();
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=".repeat(60));
        System.out.println("TCP State Machine Simulator");
        System.out.println("=".repeat(60));
        System.out.println("Valid events: PASSIVE, ACTIVE, SYN, SYNACK, ACK,");
        System.out.println("              RDATA, SDATA, FIN, CLOSE, TIMEOUT");
        System.out.println("Events are case-insensitive and space-separated");
        System.out.println("Enter Ctrl+D (Unix/Mac) or Ctrl+Z (Windows) to end input");
        System.out.println("=".repeat(60));
        System.out.println("Initial state: " + fsm.getCurrentStateName());
        System.out.println();
        
        // Read and process events from standard input
        while (scanner.hasNext()) {
            String input = scanner.next().trim().toUpperCase();
            
            // Skip empty input
            if (input.isEmpty()) {
                continue;
            }
            
            System.out.println("\nProcessing event: " + input);
            fsm.processEvent(input);
            System.out.println("Current state: " + fsm.getCurrentStateName());
        }
        
        scanner.close();
        System.out.println("\n" + "=".repeat(60));
        System.out.println("End of input. Final state: " + fsm.getCurrentStateName());
        System.out.println("Total data events processed: " + fsm.dataCount);
        System.out.println("=".repeat(60));
    }
}
