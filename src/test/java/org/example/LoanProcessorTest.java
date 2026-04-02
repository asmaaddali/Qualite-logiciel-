package bank;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoanProcessorTest {

    @Mock
    private LoanApprovalService mockApprovalService;

    @Mock
    private LoanCalculator mockCalculator;

    @Mock
    private AuditLogger mockLogger;

    @Captor
    private ArgumentCaptor<String> operationCaptor;

    @Captor
    private ArgumentCaptor<Double> amountCaptor;

    @Captor
    private ArgumentCaptor<String> borrowerIdCaptor;

    private LoanProcessor loanProcessor;

    @BeforeEach
    public void setUp() {
        loanProcessor = new LoanProcessor(
                mockApprovalService,
                mockCalculator,
                mockLogger
        );
    }

    @Test
    public void approveLoan_doitCalculerMensualite_quandApprouve() {
        when(mockApprovalService.approveLoan("JOHN", 100000.0))
                .thenReturn(true);
        when(mockCalculator.calculateMonthlyPayment(100000.0, 240))
                .thenReturn(583.56);

        LoanResult result = loanProcessor.approveLoan("JOHN", 100000.0, 240);

        verify(mockCalculator).calculateMonthlyPayment(100000.0, 240);
        assertTrue(result.approved());
        assertEquals(583.56, result.monthlyPayment());
    }

    @Test
    public void approveLoan_neDoitPasCalculer_quandRefuse() {
        when(mockApprovalService.approveLoan("JANE", 50000.0))
                .thenReturn(false);

        LoanResult result = loanProcessor.approveLoan("JANE", 50000.0, 120);

        verify(mockCalculator, never())
                .calculateMonthlyPayment(anyDouble(), anyInt());
        assertFalse(result.approved());
        assertEquals(0.0, result.monthlyPayment());
    }

    @Test
    public void approveLoan_doitJournaliserApprouvedAvecBonsParams() {
        when(mockApprovalService.approveLoan("ALICE", 200000.0))
                .thenReturn(true);
        when(mockCalculator.calculateMonthlyPayment(200000.0, 360))
                .thenReturn(1000.0);

        LoanResult result = loanProcessor.approveLoan("ALICE", 200000.0, 360);

        verify(mockLogger).log(
                operationCaptor.capture(),
                amountCaptor.capture(),
                borrowerIdCaptor.capture()
        );

        assertEquals("LOAN_APPROVED", operationCaptor.getValue());
        assertEquals(1000.0, amountCaptor.getValue());
        assertEquals("ALICE", borrowerIdCaptor.getValue());
        assertTrue(result.approved());
    }

    @Test
    public void approveLoan_doitJournaliserRejectedAvecBonsParams() {
        when(mockApprovalService.approveLoan("BOB", 300000.0))
                .thenReturn(false);

        LoanResult result = loanProcessor.approveLoan("BOB", 300000.0, 120);

        verify(mockLogger).log(
                operationCaptor.capture(),
                amountCaptor.capture(),
                borrowerIdCaptor.capture()
        );

        assertEquals("LOAN_REJECTED", operationCaptor.getValue());
        assertEquals(300000.0, amountCaptor.getValue());
        assertEquals("BOB", borrowerIdCaptor.getValue());
        assertFalse(result.approved());
    }

    @Test
    public void approveLoan_doitAppelerDansBonOrdre() {
        when(mockApprovalService.approveLoan("CHARLIE", 50000.0))
                .thenReturn(true);
        when(mockCalculator.calculateMonthlyPayment(50000.0, 60))
                .thenReturn(899.0);

        InOrder inOrder = inOrder(
                mockApprovalService,
                mockCalculator,
                mockLogger
        );

        LoanResult result = loanProcessor.approveLoan("CHARLIE", 50000.0, 60);

        inOrder.verify(mockApprovalService)
                .approveLoan("CHARLIE", 50000.0);
        inOrder.verify(mockCalculator)
                .calculateMonthlyPayment(50000.0, 60);
        inOrder.verify(mockLogger)
                .log("LOAN_APPROVED", 899.0, "CHARLIE");

        assertEquals(899.0, result.monthlyPayment());
    }

    @Test
    public void approveLoan_doitGerer2AppelsSuccessifs() {
        when(mockApprovalService.approveLoan("USER1", 10000.0))
                .thenReturn(true);
        when(mockCalculator.calculateMonthlyPayment(10000.0, 12))
                .thenReturn(847.0);

        when(mockApprovalService.approveLoan("USER2", 50000.0))
                .thenReturn(false);

        LoanResult result1 = loanProcessor.approveLoan("USER1", 10000.0, 12);
        LoanResult result2 = loanProcessor.approveLoan("USER2", 50000.0, 60);

        verify(mockCalculator, times(1))
                .calculateMonthlyPayment(anyDouble(), anyInt());

        verify(mockLogger, times(2))
                .log(anyString(), anyDouble(), anyString());

        assertTrue(result1.approved());
        assertEquals(847.0, result1.monthlyPayment());
        assertFalse(result2.approved());
    }


}