//semaphore class for synchronization control
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

class Semaphore {
    protected int value = 0;
    protected Semaphore() { value = 0; }
    protected Semaphore(int initial) { value = initial; }

    public synchronized void Wait() {
        value--;
        if (value < 0)
            try { wait(); } catch (InterruptedException e) { System.out.println("Error: " + e); }
    }

    public synchronized void signal() {
        value++;
        if (value <= 0) notify();
    }

    public synchronized int getValue() { return value; }
}

//gui class for visual display of pumps, queue, and log
class GUI {
    private static JTextArea logArea;
    private static JLabel[] pumpLabels;
    private static JPanel queuePanel;

    public static void setup(int pumps) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Service Station Simulation");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(700, 500);
            frame.setLayout(new BorderLayout(8,8));

            // top: pumps
            JPanel pumpsPanel = new JPanel(new GridLayout(1, pumps, 8, 8));
            pumpLabels = new JLabel[pumps];
            for (int i = 0; i < pumps; i++) {
                pumpLabels[i] = new JLabel("Pump " + (i + 1) + " - Free", SwingConstants.CENTER);
                pumpLabels[i].setOpaque(true);
                pumpLabels[i].setBackground(Color.GREEN);
                pumpLabels[i].setFont(new Font("Arial", Font.BOLD, 13));
                pumpsPanel.add(pumpLabels[i]);
            }

            // middle: queue
            queuePanel = new JPanel();
            queuePanel.setBorder(BorderFactory.createTitledBorder("Waiting Queue"));
            queuePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
            queuePanel.setPreferredSize(new Dimension(0, 120));

            // bottom: log
            logArea = new JTextArea();
            logArea.setEditable(false);
            JScrollPane scroll = new JScrollPane(logArea);
            scroll.setBorder(BorderFactory.createTitledBorder("Event Log"));

            frame.add(pumpsPanel, BorderLayout.NORTH);
            frame.add(queuePanel, BorderLayout.CENTER);
            frame.add(scroll, BorderLayout.SOUTH);

            frame.setVisible(true);
        });
    }

    public static void log(String msg) {
        System.out.println(msg); // print to terminal
        SwingUtilities.invokeLater(() -> {
            if (logArea != null) {
                logArea.append(msg + "\n");
                logArea.setCaretPosition(logArea.getDocument().getLength());
            }
        });
    }

    public static void updatePump(int id, String status, boolean busy) {
        SwingUtilities.invokeLater(() -> {
            if (pumpLabels != null && id - 1 < pumpLabels.length) {
                pumpLabels[id - 1].setText("Pump " + id + " - " + status);
                pumpLabels[id - 1].setBackground(busy ? Color.RED : Color.GREEN);
            }
        });
    }

    public static void updateQueue(Queue<Car> q) {
        SwingUtilities.invokeLater(() -> {
            if (queuePanel == null) return;
            queuePanel.removeAll();
            for (Car c : q) {
                JLabel lbl = new JLabel("Car " + c.getID());
                lbl.setOpaque(true);
                lbl.setBackground(Color.YELLOW);
                lbl.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                queuePanel.add(lbl);
            }
            queuePanel.revalidate();
            queuePanel.repaint();
        });
    }
}

// pump thread representing a service bay
class Pump extends Thread {
    Semaphore Empty, Full, Pump, Mutex;
    Queue<Car> queue;
    int ID;

    public Pump(int ID, Semaphore Empty, Semaphore Full, Semaphore Pump, Semaphore Mutex, Queue<Car> queue) {
        this.Empty = Empty;
        this.Full = Full;
        this.Pump = Pump;
        this.Mutex = Mutex;
        this.queue = queue;
        this.ID = ID;
    }

    @Override
    public void run() {
        //while true,,, to keep the pumps active and waiting for any car in the queue
        while(true) {
            //wait until there is any cars add to the full semaphore
            Full.Wait();
            //mutual exclusion semaphore to access queue one at a time
            Mutex.Wait();
            Car c = queue.poll() ;
            Mutex.signal();
            //signal for any waiting

            GUI.log("Pump " + ID + ": Car " + c.getID() + " occupied");
            GUI.updatePump(ID, "Occupied by Car " + c.getID(), true);

            Pump.Wait();
            GUI.log("Pump " + ID + ": Car " + c.getID() + " login");
            GUI.log("Pump " + ID + ": Car " + c.getID() + " is receiving service at bay " + ID);

            try { Thread.sleep(2000); } catch (InterruptedException e) { }

            GUI.log("Pump " + ID + ": Car " + c.getID() + " finishes service");
            Pump.signal();
            GUI.log("Pump " + ID + ": Bay " + ID + " is now free");
            GUI.updatePump(ID, "Free", false);

            // stop condition: queue empty
            synchronized (queue) {
                if (queue.isEmpty()) break;
            }
        }
    }
}

// car thread representing a customer car
class Car extends Thread {
    Semaphore Empty, Full, Pump, Mutex;
    Queue<Car> queue;
    int ID;

    public Car(int ID, Semaphore Empty, Semaphore Full, Semaphore Pump, Semaphore Mutex, Queue<Car> queue) {
        this.Empty = Empty;
        this.Full = Full;
        this.Pump = Pump;
        this.Mutex = Mutex;
        this.queue = queue;
        this.ID = ID;
    }

    public int getID() { return ID; }

    @Override
    public void run() {
        //Wait for an empty spot in the waiting area
        Empty.Wait();
        GUI.log("Car " + ID + " arrived at the service station.");

        Mutex.Wait();
        queue.add(this);
        GUI.updateQueue(queue);
        if (!queue.isEmpty()) {
            GUI.log("Car " + ID + " is waiting in the queue. Queue size: " + queue.size());
        }
        Mutex.signal();
        //signal that there is a car waiting
        Full.signal();
    }
}

// main class to start simulation
public class ServiceStation {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter waiting area capacity (1-10): ");
        int queueSize = scanner.nextInt();

        System.out.print("Enter number of pumps: ");
        int pumpNumber = scanner.nextInt();

        if (queueSize < 1 || queueSize > 10) {
            System.out.println("Error: Queue size must be between 1 and 10");
            scanner.close();
            return;
        }

        GUI.setup(pumpNumber);
        // wait a bit before simulation starts
        try { Thread.sleep(5000); } catch (InterruptedException e) { }

        Queue<Car> queue = new LinkedList<>();
        Semaphore empty = new Semaphore(queueSize);
        Semaphore full = new Semaphore(0);
        Semaphore pump = new Semaphore(pumpNumber);
        Semaphore mutex = new Semaphore(1);

        List<Pump> pumps = new ArrayList<>();
        List<Car> cars = new ArrayList<>();

        for (int i = 1; i <= pumpNumber; i++) {
            Pump p = new Pump(i, empty, full, pump, mutex, queue);
            pumps.add(p);
            p.start();
        }

        for (int i = 1; i <= queueSize; i++) {
            Car c = new Car(i, empty, full, pump, mutex, queue);
            cars.add(c);
            c.start();
        }

        for (Car car : cars) {
            try {
                car.join();
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                System.out.println("Error waiting for car: " + e);
            }
        }

        for (Pump p : pumps) {
            try {
                p.join(2000);
            } catch (InterruptedException e) {
                System.out.println("Error waiting for pump: " + e);
            }
        }

        GUI.log("All cars processed; simulation ends");
        scanner.close();
    }
}
