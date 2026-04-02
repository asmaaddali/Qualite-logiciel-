package bank;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class TransferServiceTest {

    private TransferService transferService;

    @Mock
    private BankAccount mockSource;

    @Mock
    private BankAccount mockTarget;

    @BeforeEach
    public void setUp() {
        transferService = new TransferService(mockSource, mockTarget);
    }

    @Test
    public void transfer_doitAppelerWithdraw_surCompteSource() {
        // ARRANGE
        double amount = 500.0;

        // ACT
        transferService.transfer(amount);

        // ASSERT
        verify(mockSource).withdraw(500.0);
    }

    @Test
    public void transfer_doitAppelerDeposit_surCompteCible() {
        // ARRANGE
        double amount = 500.0;

        // ACT
        transferService.transfer(amount);

        // ASSERT
        verify(mockTarget).deposit(500.0);
    }

    @Test
    public void transfer_doitAppelerWithdrawAvantDeposit() {
        // ARRANGE
        InOrder inOrder = inOrder(mockSource, mockTarget);
        double amount = 500.0;

        // ACT
        transferService.transfer(amount);

        // ASSERT
        inOrder.verify(mockSource).withdraw(500.0);
        inOrder.verify(mockTarget).deposit(500.0);
    }
}