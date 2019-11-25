import java.io.IOException;
import java.util.Scanner;

public class ATM {
    
    private Scanner in;
    private BankAccount activeAccount;
    private Bank bank;
    
    public static final int FIRST_NAME_WIDTH = 20;
    public static final int LAST_NAME_WIDTH = 30;
    
    enum Status {
    	VIEW,
    	DEPOSIT,
    	WITHDRAW,
    	TRANSFER,
    	LOGOUT,
    	DELETE,
    	S_INVALID;
    }
    
    public static final int INVALID = 0;
    public static final int INSUFFICIENT = 1;
    public static final int SUCCESS = 2;
    
    public void startup() {
        System.out.println("Welcome to the AIT ATM!");
        
        while (true) {
        	long accountNo = enterAccountNo();
        	
        	System.out.print("PIN        : ");
            int pin = in.nextInt();
            in.nextLine();
        	
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
	                    case DELETE: validLogin = deleteAccount(); break;
	                    default: System.out.println("\nInvalid selection.\n"); break;
	                }
	            }
	        } else {
	        	if (accountNo == -1 && pin == -1) {
	        		shutdown();
	        	} else {
	        		System.out.println("\nInvalid account number and/or PIN.");
	        	}
	        }
        }
    }
    
    private boolean deleteAccount() {
    	System.out.print("\nRe-type account number: ");
    	long accountNo = in.nextLong();
    	in.nextLine();
    	System.out.print("Re-type PIN: ");
    	int pin = in.nextInt();
    	in.nextLine();
    	if (accountNo == activeAccount.getAccountNo() && pin == activeAccount.getPin()) {
    		bank.deleteAccount(bank.getIndex(accountNo));
    		System.out.println("\nAccount successfully deleted. Come again soon!");
        	bank.save();
    		return false;
    	} else {
    		System.out.println("\nAccount unsuccessfully deleted. Invalid account number and/or PIN.\n");
    		return true;
    	}
    }
    
    private long enterAccountNo() {
    	System.out.print("\nAccount No.: ");
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
    
    public Status getSelection() {
    	System.out.println("[1] View balance");
        System.out.println("[2] Deposit money");
        System.out.println("[3] Withdraw money");
        System.out.println("[4] Transfer money");
        System.out.println("[5] Logout");
        System.out.println("[6] Delete account");
        
        switch (in.nextInt()) {
        	case 1: return Status.VIEW;
        	case 2: return Status.DEPOSIT;
        	case 3: return Status.WITHDRAW;
        	case 4: return Status.TRANSFER;
        	case 5: return Status.LOGOUT;
        	case 6: return Status.DELETE;
        	default: return Status.S_INVALID;
        }
    }
    
    public void showBalance() {
        System.out.println("\nCurrent balance: " + activeAccount.getBalance() + "\n");
    }
    
    public void transfer() {
    	System.out.print("\nEnter account: ");
    	long transferAccountNo = in.nextLong();
    	in.nextLine();
    	System.out.print("Enter amount: ");
    	double transferAmount = in.nextDouble();
    	in.nextLine();
    	
    	BankAccount receivingBankAccount = bank.getAccount(transferAccountNo);
    	if (receivingBankAccount == null) {
    		System.out.println("\nTransfer rejected. Destination account not found.\n");
    	} else if (receivingBankAccount == activeAccount) {
    		System.out.println("\nTransfer rejected. Destination account matches origin.\n");
    	} else if (transferAmount == 0) {
    		System.out.println("\nTransfer rejected. Amount must be greater than $0.00.\n");
    	} else {
    		double originalActiveBalance = activeAccount.getBalanceDouble();
        	activeAccount.withdraw(transferAmount);
        	
        	if (originalActiveBalance == (activeAccount.getBalanceDouble() + transferAmount)) {
        		double originalRecBalance = receivingBankAccount.getBalanceDouble();
        		receivingBankAccount.deposit(transferAmount);
        		
        		if (originalRecBalance == (receivingBankAccount.getBalanceDouble() - transferAmount)) {
        			System.out.println("\nTransfer accepted.\n");
        		} else {
        			System.out.println("\nTransfer rejected. Amount would cause destination balance to exceed $999,999,999,999.99.\n");
        			activeAccount.deposit(transferAmount);
        		}
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
        in.nextLine();
        
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
        in.nextLine();
        
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
    	BankAccount lastAccount = bank.getAccounts().get(bank.getAccounts().size() - 1);
    	if (lastAccount.getAccountNo() < 999999999) {
    		String firstName = "";
        	do {
        		System.out.print("\nFirst name: ");
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
    	    	in.nextLine();
    	    	if (pinHolder >= 1000 && pinHolder <= 9999) {
    	    		pin = pinHolder;
    	    	}
        	} while (pin < 1000 || pin > 9999);
        	
        	BankAccount newAccount = bank.createAccount(pin, newUser);
        	
        	System.out.println("\nThank you. You account number is " + newAccount.getAccountNo() + ".");
        	System.out.println("Please login to access your newly created account.\n");
        	System.out.println("Account No.: " + newAccount.getAccountNo());
        	
        	bank.update(newAccount);
        	bank.save();
        	return newAccount.getAccountNo();
    	} else {
    		System.out.println("\nCreation rejected. Too many bank accounts already in use.\n");
    		return 0;
    	}
    }
    
    /**
     * Constructs a new instance of the ATM class.
     */
    
    public ATM() {
        this.in = new Scanner(System.in);
        
        try {
			this.bank = new Bank();
		} catch (IOException e) {
			shutdown();
		}
    }
    
    /*
     * Application execution begins here.
     */
    
    public static void main(String[] args) {    	
    	    	
    	ATM atm = new ATM();
    	               
        atm.startup();
    }
}
