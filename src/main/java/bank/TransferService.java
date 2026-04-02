package bank;

public class TransferService {
    private final BankAccount sourceAccount;
    private final BankAccount targetAccount;

    // ✅ SOLUTION : injection par constructeur
    public TransferService(BankAccount sourceAccount,
                           BankAccount targetAccount) {
        this.sourceAccount = sourceAccount;
        this.targetAccount = targetAccount;
    }

    public void transfer(double amount) {
        sourceAccount.withdraw(amount);
        targetAccount.deposit(amount);
    }
}