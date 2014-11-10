import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * This class provides a simple simulation of a deadlock.
 * The app inherits the deadlock detector mechanism from AppWrapper.
 *
 */
public class DeadlockedApp extends AppWrapper {

	public static class Account {
		double balance;
		String id = UUID.randomUUID().toString();
		Lock locker = new ReentrantLock();

		void withdraw(double amount) {
			balance -= amount;
		}

		void deposit(double amount) {
			balance += amount;
		}
	}

	/**
	 * This function will transfer money from the @from Account to the @to Account.
	 * It uses the lock for each Account to ensure transaction semantics for
	 * gate-keeping to that Account.
	 */
	void transfer(Account from, Account to, double amount) {
		from.locker.lock();
		to.locker.lock();

		from.withdraw(amount);
		to.deposit(amount);

		to.locker.unlock();
		from.locker.unlock();
	}

	/**
	 * Dummy function to start the real problematic App.
	 * You can see the bug when the 2 threads here are trying to transfer money.
	 * The proper way is to lock in the correct order in any case, so the
	 * solution would be to sort the given Accounts based on the id and get the
	 * locks in-order no matter what the user provides.
	 */
	public void runme() {
		final Account from = new Account();
		final Account to = new Account();

		new Thread() {
			@Override
			public void run() {
				while(true) {
					transfer(from, to, 0);
				}
			}
		}.start();

		new Thread() {
			@Override
			public void run() {
				while(true) {
					transfer(to, from, 1);
				}
			}
		}.start();
	}

	public static void main(String[] args) throws Exception {
		new DeadlockedApp().runme();
	}
}
