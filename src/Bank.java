import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Bank {
	
	/*
	 * Accounts are read from a fixed-width file. Each account is represented
	 * as a single line of text. These start and end indexes help to parse the
	 *line of text into individual account fields.
	 */
    
    private final static int ACCT_START = 0;
    private final static int ACCT_END = 9;
    private final static int PIN_START = 9;
    private final static int PIN_END = 13;
    private final static int FIRST_NAME_START = 13;
    private final static int FIRST_NAME_END = 33;
    private final static int LAST_NAME_START = 33;
    private final static int LAST_NAME_END = 63;
    private final static int BALANCE_START = 63;
    
    private final static String DATA = "data/accounts.dat";		// data file path
    
    private List<BankAccount> accounts;							// an in-memory list of BankAccount objects
    
    public List<BankAccount> getAccounts() {
    	return accounts;
    }
    
    /**
     * Constructs a new instance of the Bank class.
     * 
     * @throws IOException in the case of a file I/O error
     */
    
    public Bank() throws IOException {
        accounts = init();
        
        if (accounts == null) {
        	throw new IOException();
        }
    }
    
    /**
     * Creates and returns a new BankAccount object.
     * 
     * @param pin the PIN to be associated with this account
     * @param user the account holder to be associated with this account
     * @return the newly created account
     */
    
    public BankAccount createAccount(int pin, User user) {
    	accounts.add(new BankAccount(pin, generateAccountNo(), user));
    	
    	return accounts.get(accounts.size() - 1);
    }
    
    /**
     * Logs into an existing account.
     * 
     * @param accountNo the account number
     * @param pin the account PIN
     * @return the BankAccount object (provided the login credentials are valid)
     */
    
    public BankAccount login(long accountNo, int pin) {
        BankAccount bankAccount = getAccount(accountNo);
        
        if(bankAccount == null) {
        	return null;
        } else if (bankAccount.getPin() == pin) {
            return bankAccount;
        } else {
            return null;
        }
    }
    
    /*
     * Retrieves an existing account by account number.
     * 
     * @param accountNo the account number to retrieve
     * @return the BankAccount object associated with that account number
     */
    
    public BankAccount getAccount(long accountNo) {
        for (BankAccount account : accounts) {
            if (account.getAccountNo() == accountNo) {
                return account;
            }
        }
        
        return null;
    }
    
    /**
     * Updates the information associated with a specific account.
     * 
     * @param account the BankAccount object to be updated
     */
    
    public void update(BankAccount account) {
        int index = -1;
        
        for (int i = 0; i < accounts.size(); i++) {
            BankAccount storedAccount = accounts.get(i);
            
            if (storedAccount.getAccountNo() == account.getAccountNo()) {
                index = i;
                break;
            }
        }
        
        accounts.set(index, account);
    }
    
    /**
     * Saves the state of any modified accounts.
     */
    
    public boolean save() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(DATA))) {
            for (BankAccount account : accounts) {
                bw.write(account.toString());
                bw.newLine();
            }
            
            return true;
        } catch (IOException e) {
        	System.err.println("Error: Unable to write to data file.");
        	
        	return false;
        }
    }
    
    /*
     * Initializes the Bank's internal list of accounts. This information is read
     * in from the accounts.dat file.
     * 
     * @return a list of BankAccount objects
     */
    
    private List<BankAccount> init() {
        List<BankAccount> accounts = new ArrayList<BankAccount>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(new File(DATA)))) {
            String account;
            
            while ((account = br.readLine()) != null) {
                accounts.add(Bank.parseBankAccount(account));
            }
        } catch (FileNotFoundException e) {
            System.err.println("Error: Unable to find data file.");
            
            accounts = null;
        } catch (IOException e) {
            System.err.println("Error: Unable to read from data file.");
            
            accounts = null;
        }
        
        return accounts;
    }
    
    /*
     * Generates the next sequential account number.
     * 
     * @return the next account number
     */
    
    private long generateAccountNo() {
        long accountNo = -1;
        
        for (BankAccount account : accounts) {
            if (account.getAccountNo() > accountNo) {
                accountNo = account.getAccountNo();
            }
        }
        
        return accountNo + 1;
    }
    
    /*
     * Parses the BankAccount object from an account string.
     * 
     * @param account a string of text representing account details
     * @return the bank account
     */
    
    private static BankAccount parseBankAccount(String account) {
        return new BankAccount(Bank.parsePin(account),
            Bank.parseAccountNo(account),
            Bank.parseBalance(account),
            Bank.parseUser(account)
        );
    }
    
    /*
     * Parses the account number from an account string.
     * 
     * @param account a string of text representing account details
     * @return the account number
     */
    
    private static long parseAccountNo(String account) {
        return Long.parseLong(account.substring(ACCT_START, ACCT_END));
    }
    
    /*
     * Parses the PIN from an account string.
     * 
     * @param account a string of text representing account details
     * @return the account PIN
     */
    
    private static int parsePin(String account) {
        return Integer.parseInt(account.substring(PIN_START, PIN_END));
    }
    
    /*
     * Parses the User object from an account string.
     * 
     * @param account a string of text representing account details
     * @return the account holder
     */
    
    private static User parseUser(String account) {        
        return new User(account.substring(FIRST_NAME_START, FIRST_NAME_END).strip(),
            account.substring(LAST_NAME_START, LAST_NAME_END).strip()
        );
    }
    
    /*
     * Parses the balance from an account string.
     * 
     * @param account a string of text representing account details
     * @return the account balance
     */
    
    private static double parseBalance(String account) {
        return Double.parseDouble(account.substring(BALANCE_START).strip());
    }
}
