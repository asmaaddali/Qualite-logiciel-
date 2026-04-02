package bank;

public class LoanProcessor {

    private final LoanApprovalService approvalService;
    private final LoanCalculator calculator;
    private final AuditLogger logger;

    public LoanProcessor(LoanApprovalService approvalService,
                         LoanCalculator calculator,
                         AuditLogger logger) {
        this.approvalService = approvalService;
        this.calculator = calculator;
        this.logger = logger;
    }

    public LoanResult approveLoan(String borrowerId, double amount, int months) {
        boolean approved = approvalService.approveLoan(borrowerId, amount);

        if (!approved) {
            logger.log("LOAN_REJECTED", amount, borrowerId);
            return new LoanResult(false, 0.0);
        }

        double monthlyPayment = calculator.calculateMonthlyPayment(amount, months);

        logger.log("LOAN_APPROVED", monthlyPayment, borrowerId);

        return new LoanResult(true, monthlyPayment);
    }
}