import java.io.IOException;
import java.util.Scanner;

public class ATM {
    
    private Scanner in;
    private BankAccount activeAccount;
    private Bank bank;
    
    public static final int FIRST_NAME_WIDTH = 20;
    public static final int LAST_NAME_WIDTH = 30;
    
    public static final int VIEW = 1;
    public static final int DEPOSIT = 2;
    public static final int WITHDRAW = 3;
    public static final int TRANSFER = 4;
    public static final int LOGOUT = 5;
    
    public static final int INVALID = 0;
    public static final int INSUFFICIENT = 1;
    public static final int SUCCESS = 2;
    
    ////////////////////////////////////////////////////////////////////////////
    //                                                                        //
    // Refer to the Simple ATM tutorial to fill in the details of this class. //
    // You'll need to implement the new features yourself.                    //
    //                                                                        //
    ////////////////////////////////////////////////////////////////////////////
    
    /**
     * Constructs a new instance of the ATM class.
     */
    
    public ATM() {
        this.in = new Scanner(System.in);
        
        try {
			this.bank = new Bank();
		} catch (IOException e) {
			shutdown();
			// cleanup any resources (i.e., the Scanner) and exit
		}
    }
    
    /*
     * Application execution begins here.
     */
    
    public void startup() {
        System.out.println("Welcome to the AIT ATM!\n");
        
        while (true) {
        	long accountNo = enterAccountNo();
        	
        	System.out.print("PIN        : ");
            int pin = in.nextInt();
        	
	        if (isValidLogin(accountNo, pin)) {
	            System.out.println("\nHello, again, " + activeAccount.getAccountHolder().getFirstName() + "!\n");
	            
	            boolean validLogin = true;
	            while (validLogin) {
	                switch (getSelection()) {
	                    case VIEW: showBalance(); break;
	                    case DEPOSIT: deposit(); break;
	                    case WITHDRAW: withdraw(); break;
	                    case TRANSFER: transfer(); break;
	                    case LOGOUT: validLogin = false; break;
	                    default: System.out.println("\nInvalid selection.\n"); break;
	                }
	            }
	        } else {
	        	if (accountNo == -1 && pin == -1) {
	        		shutdown();
	        	} else {
	        		System.out.println("\nInvalid account number and/or PIN.\n");
	        	}
	        }
        }
    }
    
    private long enterAccountNo() {
    	System.out.print("Account No.: ");
        String accountNoString = in.next();
        long accountNo = 0;
        if (accountNoString.equals("+")) {
        	accountNo = makeAccount();
        } else {
        	accountNo = Long.valueOf(accountNoString);
        }
        return accountNo;
    }
    
    public boolean isValidLogin(long accountNo, int pin) {
    	activeAccount = bank.login(accountNo, pin);
    	return activeAccount != null;
    }
    
    public int getSelection() {
        System.out.println("[1] View balance");
        System.out.println("[2] Deposit money");
        System.out.println("[3] Withdraw money");
        System.out.println("[4] Transfer money");
        System.out.println("[5] Logout");
        
        return in.nextInt();
    }
    
    public void showBalance() {
        System.out.println("\nCurrent balance: " + activeAccount.getBalance() + "\n");
    }
    
    public void transfer() {
    	System.out.print("Enter account: ");
    	long transferAccountNo = in.nextLong();
    	System.out.print("Enter amount: ");
    	double transferAmount = in.nextDouble();
    	
    	BankAccount receivingBankAccount = bank.getAccount(transferAccountNo);
    	if (receivingBankAccount == null) {
    		System.out.println("\nTransfer rejected. Destination account not found.\n4");
    	} else {
    		double originalActiveBalance = activeAccount.getBalanceDouble();
        	activeAccount.withdraw(transferAmount);
        	if (originalActiveBalance == (activeAccount.getBalanceDouble() + transferAmount)) {
        		receivingBankAccount.deposit(transferAmount);
            	System.out.println("\nTransfer accepted.\n");
        	} else {
        		System.out.println("\nTransfer rejected due to insufficient funds.\n");
        	}
        	
        	bank.update(activeAccount);
        	bank.update(receivingBankAccount);
        	bank.save();
    	}
    }
    
    public void deposit() {
        System.out.print("\nEnter amount: ");
        double amount = in.nextDouble();
        
        int status = activeAccount.deposit(amount);
        if (status == ATM.INVALID) {
            if (activeAccount.getBalanceDouble() > BankAccount.MAX_BALANCE) {
            	System.out.println("\nDeposit rejected. Amount would cause balance to exceed $999,999,999,999.99.\n");
            } else {
            	System.out.println("\nDeposit rejected. Amount must be greater than $0.00.\n");
            }
        } else if (status == ATM.SUCCESS) {
            System.out.println("\nDeposit accepted.\n");
        }
        
        bank.update(activeAccount);
    	bank.save();
    }
    
    public void withdraw() {
        System.out.print("\nEnter amount: ");
        double amount = in.nextDouble();
        
        int status = activeAccount.withdraw(amount);
        if (status == ATM.INVALID) {
            System.out.println("\nWithdrawal rejected. Amount must be greater than $0.00.\n");
        } else if (status == ATM.INSUFFICIENT) {
            System.out.println("\nWithdrawal rejected. Insufficient funds.\n");
        } else if (status == ATM.SUCCESS) {
            System.out.println("\nWithdrawal accepted.\n");
        }
        
        bank.update(activeAccount);
    	bank.save();
    }
    
    public void shutdown() {
    	if (in != null) {
    		in.close();
    	}
    	
    	System.out.println("\nGoodbye!");
        System.exit(0);
    }
    
    public long makeAccount() {
    	
    	String firstName = "";
    	do {
    		System.out.print("First name: ");
    		firstName = in.next();
    	} while (firstName.length() < 0 || firstName.length() > ATM.FIRST_NAME_WIDTH);
    	
    	String lastName = "";
    	do {
    		System.out.print("Last name: ");
    		lastName = in.next();
    	} while (lastName.length() < 0 || lastName.length() > ATM.LAST_NAME_WIDTH);
    	
    	User newUser = new User(firstName, lastName);
    	
    	int pin = 0;
    	do {
	    	System.out.print("PIN: ");
	    	int pinHolder = in.nextInt();
	    	if (pinHolder >= 1000 && pinHolder <= 9999) {
	    		pin = pinHolder;
	    	}
    	} while (pin < 1000 || pin > 9999);
    	
    	BankAccount newAccount = bank.createAccount(pin, newUser);
    	
    	System.out.println("\nThank you. You account number is " + newAccount.getAccountNo() + ".");
    	System.out.println("Please login to access your newly created account.\n");
    	System.out.println("Account no: " + newAccount.getAccountNo());
    	
    	bank.update(newAccount);
    	bank.save();
    	return newAccount.getAccountNo();
    }
    
    public static void main(String[] args) {    	
    	    	
    	ATM atm = new ATM();
    	               
        atm.startup();
    }
}
