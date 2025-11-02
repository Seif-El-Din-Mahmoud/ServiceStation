import java.util.Queue;

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
        try {
            //Wait for an empty spot in the waiting area
            Empty.Wait();
            System.out.println("Car " + ID + " arrived at the service station.");

            //Add car to the queue
            Mutex.Wait();
            queue.add(this);
            System.out.println("Car " + ID + " is waiting in the queue. Queue size: " + queue.size());
            Mutex.signal();

            //Signal that there is a car or more waiting
            Full.signal();

            //Wait for an available pump
            Pump.Wait();
            System.out.println("Car " + ID + " is being serviced.");

            //Simulate time taken to service the car
            Thread.sleep(2000);

            //Finish servicing
            System.out.println("Car " + ID + " has been serviced and is leaving the station .");

            //Free up the pump
            Pump.signal();
            Mutex.Wait();
            queue.remove(this);
            Mutex.signal();

            //Signal that there is an empty spot in the waiting area
            Empty.signal();

        } catch (InterruptedException e) {
            System.out.println("Error in the car: " + ID + ":" + e.getMessage());
        }
    }
}

public class ServiceStation {
    public static void main(String[] args) {
        System.out.printf("Hello and welcome!");

    }
}