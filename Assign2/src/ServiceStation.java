import java.net.SocketOption;
import java.util.*;

class Semaphore {
    protected int value = 0 ;
    protected Semaphore() { value = 0 ; }
    protected Semaphore(int initial) { value = initial ; }
    public synchronized void Wait() {
        value-- ;
        if (value < 0)
            try { wait();}
        catch( InterruptedException e )
        {System.out.println("Error: " + e);}
    }
    public synchronized void signal() {
        value++ ; if (value <= 0) notify() ;
    }
    public synchronized int getValue() { return value ; }
}

class Pump extends Thread {
    Semaphore Empty,Full,Pump,Mutex;
    Queue<Car> queue;
    int ID;
    public Pump(int ID, Semaphore Empty, Semaphore Full, Semaphore Pump, Semaphore Mutex, Queue<Car> queue) {
        this.Empty = Empty ;
        this.Full = Full ;
        this.Pump = Pump ;
        this.Mutex = Mutex ;
        this.queue = queue ;
        this.ID = ID ;
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
            //signal for any waiting cars that there is an empty slot in the queue
            Empty.signal();

            System.out.println("Pump " + ID + ": Car " + c.getID() + " occupied");

            //occupy a pump
            Pump.Wait();
            System.out.println("Pump " + ID + ": Car " + c.getID() + " login");
            System.out.println("Pump " + ID + ": Car " + c.getID() + " is recieving service at bay" + ID);
            try{
                Thread.sleep(2000);
            }
            catch(InterruptedException e){
                System.out.println("Error: " + e);
            }
            System.out.println("Pump " + ID + ": Car " + c.getID() + " finishes service");
            Pump.signal();
            System.out.println("Pump " + ID + ": Bay " + ID + " is now free");

                // stop condition: queue empty
                synchronized (queue) {
                    if (queue.isEmpty()) {
                        break;
                    }

                }

      }

    }
    public int getID() { return ID ;}
}

class Car extends Thread {
    Semaphore Empty,Full,Pump,Mutex;
    Queue<Car> queue;
    int ID;
    public Car(int ID, Semaphore Empty, Semaphore Full, Semaphore Pump, Semaphore Mutex, Queue<Car> queue) {
        this.Empty = Empty ;
        this.Full = Full ;
        this.Pump = Pump ;
        this.Mutex = Mutex ;
        this.queue = queue ;
        this.ID = ID ;
    }
    public int getID() { return ID ;}

    @Override
    public void run() {
        //Wait for an empty spot in the waiting area
        Empty.Wait();
        System.out.println("Car " + ID + " arrived at the service station.");

        //Add car to the queue
        Mutex.Wait();
        queue.add(this);
        //if the pumps are full the car will have to wait
        System.out.println("Car " + ID + " is waiting in the queue. Queue size: " + queue.size());

        Mutex.signal();

        //Signal that there is a car or more waiting
        Full.signal();

    }
}

public class ServiceStation {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter waiting area capacity (1-10): ");
        int queueSize = scanner.nextInt();

        System.out.print("Enter number of pumps: ");
        int pumpNumber = scanner.nextInt();

        // alidate input
        if (queueSize < 1 || queueSize > 10) {
            System.out.println("Error: Queue size must be between 1 and 10");
            scanner.close();
            return;
        }

        //initialize queue and semaphores
        Queue<Car> queue = new LinkedList<>();
        Semaphore empty = new Semaphore(queueSize);
        Semaphore full = new Semaphore(0);
        Semaphore pump = new Semaphore(pumpNumber);
        Semaphore mutex = new Semaphore(1);

        List<Pump> pumps = new ArrayList<>();
        List<Car> cars = new ArrayList<>();

        // create and start pumps
        for (int i = 1; i <= pumpNumber; i++) {
            Pump p = new Pump(i, empty, full, pump, mutex, queue);
            pumps.add(p);
            p.start();
        }

        // create cars
        for (int i = 1; i <= queueSize; i++) {
            Car c = new Car(i, empty, full, pump, mutex, queue);
            cars.add(c);
            c.start();


        }

        // wait for all cars to be processed
        for (Car car : cars) {
            try {
                car.join();
            } catch (InterruptedException e) {
                System.out.println("Error waiting for car: " + e);
            }
        }

        // wait for pumps to finish
        for (Pump p : pumps) {
            try {
                p.join(2000); // wait up to 2 seconds for pump to finish
            } catch (InterruptedException e) {
                System.out.println("Error waiting for pump: " + e);
            }
        }

        System.out.println("All cars processed; simulation ends");
        scanner.close();
    }

}