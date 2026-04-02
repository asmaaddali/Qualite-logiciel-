package bank;

public record LoanResult(boolean approved, double monthlyPayment) {

    public LoanResult {
        if (monthlyPayment < 0) {
            throw new IllegalArgumentException("La mensualité ne peut pas être négative");
        }
        if (!approved && monthlyPayment > 0) {
            throw new IllegalArgumentException("Une mensualité ne peut pas être définie si le prêt est refusé");
        }
    }

    public String getStatus() {
        return approved ? "APPROVED" : "REJECTED";
    }
}
