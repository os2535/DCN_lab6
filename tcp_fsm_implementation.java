import Fsm.*;
import java.util.Scanner;


public class TCPStateMachine {
    
    private FSM fsm;
    private int dataCount = 0;
    
    // State objects
    private State closed;
    private State listen;
    private State synSent;
    private State synRcvd;
    private State established;
    private State finWait1;
    private State finWait2;
    private State closing;
    private State timeWait;
    private State closeWait;
    private State lastAck;
    
    // Event objects
    private Event passiveEvent;
    private Event activeEvent;
    private Event synEvent;
    private Event synAckEvent;
    private Event ackEvent;
    private Event rdataEvent;
    private Event sdataEvent;
    private Event finEvent;
    private Event closeEvent;
    private Event timeoutEvent;
    
   
    public TCPStateMachine() {
        createStates();
        createEvents();
        createFSM();
        addTransitions();
    }
    
    
    private void createStates() {
        closed = new State("CLOSED");
        listen = new State("LISTEN");
        synSent = new State("SYN_SENT");
        synRcvd = new State("SYN_RCVD");
        established = new State("ESTABLISHED");
        finWait1 = new State("FIN_WAIT_1");
        finWait2 = new State("FIN_WAIT_2");
        closing = new State("CLOSING");
        timeWait = new State("TIME_WAIT");
        closeWait = new State("CLOSE_WAIT");
        lastAck = new State("LAST_ACK");
    }
    
    
    private void createEvents() {
        passiveEvent = new Event("PASSIVE");
        activeEvent = new Event("ACTIVE");
        synEvent = new Event("SYN");
        synAckEvent = new Event("SYNACK");
        ackEvent = new Event("ACK");
        rdataEvent = new Event("RDATA");
        sdataEvent = new Event("SDATA");
        finEvent = new Event("FIN");
        closeEvent = new Event("CLOSE");
        timeoutEvent = new Event("TIMEOUT");
    }
    
    
    private void createFSM() {
        fsm = new FSM("TCP_FSM", closed);
    }
    
    
    private void addTransitions() {
        // From CLOSED
        fsm.addTransition(new Transition(closed, passiveEvent, listen, 
            new StandardAction("PASSIVE", "CLOSED")));
        fsm.addTransition(new Transition(closed, activeEvent, synSent, 
            new StandardAction("ACTIVE", "CLOSED")));
        
        // From LISTEN
        fsm.addTransition(new Transition(listen, synEvent, synRcvd, 
            new StandardAction("SYN", "LISTEN")));
        fsm.addTransition(new Transition(listen, closeEvent, closed, 
            new StandardAction("CLOSE", "LISTEN")));
        
        // From SYN_SENT
        fsm.addTransition(new Transition(synSent, synEvent, synRcvd, 
            new StandardAction("SYN", "SYN_SENT")));
        fsm.addTransition(new Transition(synSent, synAckEvent, established, 
            new StandardAction("SYNACK", "SYN_SENT")));
        fsm.addTransition(new Transition(synSent, closeEvent, closed, 
            new StandardAction("CLOSE", "SYN_SENT")));
        
        // From SYN_RCVD
        fsm.addTransition(new Transition(synRcvd, ackEvent, established, 
            new StandardAction("ACK", "SYN_RCVD")));
        fsm.addTransition(new Transition(synRcvd, closeEvent, finWait1, 
            new StandardAction("CLOSE", "SYN_RCVD")));
        
        // From ESTABLISHED - special handling for data events
        fsm.addTransition(new Transition(established, rdataEvent, established, 
            new DataAction("RDATA", this)));
        fsm.addTransition(new Transition(established, sdataEvent, established, 
            new DataAction("SDATA", this)));
        fsm.addTransition(new Transition(established, finEvent, closeWait, 
            new StandardAction("FIN", "ESTABLISHED")));
        fsm.addTransition(new Transition(established, closeEvent, finWait1, 
            new StandardAction("CLOSE", "ESTABLISHED")));
        
        // From FIN_WAIT_1
        fsm.addTransition(new Transition(finWait1, finEvent, closing, 
            new StandardAction("FIN", "FIN_WAIT_1")));
        fsm.addTransition(new Transition(finWait1, ackEvent, finWait2, 
            new StandardAction("ACK", "FIN_WAIT_1")));
        
        // From FIN_WAIT_2
        fsm.addTransition(new Transition(finWait2, finEvent, timeWait, 
            new StandardAction("FIN", "FIN_WAIT_2")));
        
        // From CLOSING
        fsm.addTransition(new Transition(closing, ackEvent, timeWait, 
            new StandardAction("ACK", "CLOSING")));
        
        // From TIME_WAIT
        fsm.addTransition(new Transition(timeWait, timeoutEvent, closed, 
            new StandardAction("TIMEOUT", "TIME_WAIT")));
        
        // From CLOSE_WAIT
        fsm.addTransition(new Transition(closeWait, closeEvent, lastAck, 
            new StandardAction("CLOSE", "CLOSE_WAIT")));
        
        // From LAST_ACK
        fsm.addTransition(new Transition(lastAck, ackEvent, closed, 
            new StandardAction("ACK", "LAST_ACK")));
    }
    
   
    private Event getEventFromString(String input) {
        switch (input) {
            case "PASSIVE": return passiveEvent;
            case "ACTIVE": return activeEvent;
            case "SYN": return synEvent;
            case "SYNACK": return synAckEvent;
            case "ACK": return ackEvent;
            case "RDATA": return rdataEvent;
            case "SDATA": return sdataEvent;
            case "FIN": return finEvent;
            case "CLOSE": return closeEvent;
            case "TIMEOUT": return timeoutEvent;
            default: return null;
        }
    }
    
   
    public void processEvent(String input) {
        Event event = getEventFromString(input);
        
        if (event == null) {
            System.out.println("Error: unexpected Event: " + input);
            return;
        }
        
        try {
            fsm.doEvent(event);
        } catch (FsmException e) {
            // Display exception as required
            System.out.println(e.toString());
        }
    }
    
   
    public int incrementDataCount() {
        return ++dataCount;
    }
    
    public String getCurrentState() {
        return fsm.currentState().name();
    }
    
    
    static class StandardAction extends Action {
        private String eventName;
        private String stateName;
        
        public StandardAction(String eventName, String stateName) {
            this.eventName = eventName;
            this.stateName = stateName;
        }
        
        public void execute() {
            System.out.println("Event " + eventName + " received, current State is " + stateName);
        }
    }
    
    /**
     * Data Action class for RDATA/SDATA in ESTABLISHED state
     * Prints: "DATA received n" or "DATA sent n"
     */
    static class DataAction extends Action {
        private String eventName;
        private TCPStateMachine machine;
        
        public DataAction(String eventName, TCPStateMachine machine) {
            this.eventName = eventName;
            this.machine = machine;
        }
        
        public void execute() {
            int count = machine.incrementDataCount();
            if (eventName.equals("RDATA")) {
                System.out.println("DATA received " + count);
            } else {
                System.out.println("DATA sent " + count);
            }
        }
    }
    

    public static void main(String[] args) {
        TCPStateMachine machine = new TCPStateMachine();
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=".repeat(60));
        System.out.println("TCP State Machine Simulator");
        System.out.println("Using FSM Package");
        System.out.println("=".repeat(60));
        System.out.println("Valid events: PASSIVE, ACTIVE, SYN, SYNACK, ACK,");
        System.out.println("              RDATA, SDATA, FIN, CLOSE, TIMEOUT");
        System.out.println("Events must be in UPPERCASE");
        System.out.println("=".repeat(60));
        System.out.println("Initial state: " + machine.getCurrentState());
        System.out.println();
        
        // Process events until EOF
        while (scanner.hasNext()) {
            String input = scanner.next().trim().toUpperCase();
            
            if (input.isEmpty()) {
                continue;
            }
            
            machine.processEvent(input);
            System.out.println("Current state: " + machine.getCurrentState());
            System.out.println();
        }
        
        scanner.close();
        System.out.println("=".repeat(60));
        System.out.println("Program terminated. Final state: " + machine.getCurrentState());
        System.out.println("=".repeat(60));
    }
}
