import java.util.Queue;

class Semaphore {
    protected int value = 0 ;
    protected Semaphore() { value = 0 ; }
    protected Semaphore(int initial) { value = initial ; }
    public synchronized void P() {
        value-- ;
        if (value < 0)
            try { wait();}
        catch( InterruptedException e )
        {System.out.println("Error: " + e);}
    }
    public synchronized void V() {
        value++ ; if (value <= 0) notify() ;
    }
}

class Pump extends Thread {
    Semaphore Empty,Full,Pump,Mutex;
    Queue<Car> queue;
    int ID;
    public Pump(int ID, Semaphore Empty, Semaphore Full, Semaphore Pump, Queue<Car> queue) {
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
    public Car(int ID, Semaphore Empty, Semaphore Full, Semaphore Pump, Queue<Car> queue) {
        this.Empty = Empty ;
        this.Full = Full ;
        this.Pump = Pump ;
        this.Mutex = Mutex ;
        this.queue = queue ;
        this.ID = ID ;
    }
    public int getID() { return ID ;}
}

public class ServiceStation {
    public static void main(String[] args) {
        System.out.printf("Hello and welcome!");

    }
}