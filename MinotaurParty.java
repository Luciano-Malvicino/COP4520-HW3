import java.util.*;
import java.util.concurrent.locks.*;

class Present {
  int tag;

  Present(int tag) {
    this.tag = tag;
  }
}

public class MinotaurParty {

  static LinkedList<Present> linkedList = new LinkedList<>();
  static List<Present> bag = new ArrayList<>();
  static ReentrantLock lock = new ReentrantLock();
	static int totalWrittenNotes = 0;

  public static void main(String[] args) {

    // Adding presents to a bag and shuffle them so its unordered
    for (int i = 1; i <= 500000; i++) {
      Present present = new Present(i);
      bag.add(present);
    }
    Collections.shuffle(bag);
    System.out.println("present bag ready.");

    // Creation of servants (threads)
    Thread[] servants = new Thread[4];
    for (int i = 0; i < 4; i++) {
      servants[i] = new Thread(new Servant(i + 1));
      servants[i].start();
    }

    // Wait for all servants (threads) to finish
    for (Thread servant : servants) {
      try {
        servant.join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

		if(totalWrittenNotes == 500000)
    	System.out.println("All thank you notes have been written.");
  }

  static class Servant implements Runnable {
    int id;

    Servant(int id) {
      this.id = id;
    }

    @Override
    public void run() {
      while (true) {
        Present present = null;
        lock.lock();
        try {
					// If there are still presents in the bag remove it from the bad and add it to the linked list
          if (!bag.isEmpty()) {
            present = bag.remove(0);
						addPresentToLinkedList(present, id);
          }
        } finally {
          lock.unlock();
        }
        if (present != null) {
					lock.lock();
					linkedList.remove(present);
					lock.unlock();
					writeThankYouNote(present, id);
        } else {
          break;
        }
      }
    }
  }

  static void addPresentToLinkedList(Present present, int id) {
    lock.lock();
    try {
      // If the chain of presents is empty, add the present directly.
      if (linkedList.isEmpty()) {
        linkedList.add(present);
        return;
      }

      // Otherwise, find the correct position to add the present.
      ListIterator<Present> iterator = linkedList.listIterator();
      while (iterator.hasNext()) {
        Present next = iterator.next();
        if (next.tag > present.tag) {
          iterator.previous();
          iterator.add(present);
          return;
        }
      }

      linkedList.addLast(present);
    } finally {
      lock.unlock();
    }
  }

  static void writeThankYouNote(Present present, int id) {
		totalWrittenNotes++;
  }
}
