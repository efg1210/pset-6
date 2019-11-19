public class User {
    
    private String firstName;
    private String lastName;
    
    private ATM ATM = new ATM();
    
    public User(String firstName, String lastName) {
		this.firstName = firstName;
		this.lastName = lastName;
	}
	
	public String getFirstName() {
		return firstName;
	}
	
	public String getLastName() {
		return lastName;
	}
    
    /**
     * Formats the first and last name in preparation to be written to the data file.
     * 
     * @return a fixed-width string in line with the data file specifications.
     */
    
    public String serialize() {
        return String.format("%1$-" + ATM.FIRST_NAME_WIDTH + "s", firstName) +
            String.format("%1$-" + ATM.LAST_NAME_WIDTH + "s", lastName);
    }
}

