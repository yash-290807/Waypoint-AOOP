package casestudy3;

import java.util.Objects;

/**
 * Domain Service managing fund transfer coordination between accounts.
 * Ensures transactional atomicity:
 * - Orders account locks to prevent deadlocks.
 * - Rolls back withdrawal if destination credit fails (compensating transaction).
 */
public class TransferService {

    public FundTransfer executeTransfer(BankAccount fromAccount, BankAccount toAccount, PositiveMoney amount) {
        Objects.requireNonNull(fromAccount, "Source account cannot be null");
        Objects.requireNonNull(toAccount, "Destination account cannot be null");
        Objects.requireNonNull(amount, "Amount cannot be null");

        FundTransfer transfer = new FundTransfer(fromAccount.getAccountNumber(), toAccount.getAccountNumber(), amount);

        // Lock ordering by Account Number to prevent circular deadlocks
        BankAccount firstLock = fromAccount.getAccountNumber().getValue().compareTo(toAccount.getAccountNumber().getValue()) < 0
                ? fromAccount : toAccount;
        BankAccount secondLock = (firstLock == fromAccount) ? toAccount : fromAccount;

        synchronized (firstLock) {
            synchronized (secondLock) {
                try {
                    // Step 1: Debit source account
                    fromAccount.transferOut(amount, toAccount.getAccountNumber(), transfer.getTransferId());

                    try {
                        // Step 2: Credit destination account
                        toAccount.transferIn(amount, fromAccount.getAccountNumber(), transfer.getTransferId());
                        transfer.markCompleted();
                    } catch (Exception ex) {
                        // Rollback / Compensating action: Refund source account if credit fails!
                        fromAccount.deposit(amount, "Reversal of transfer failure [Ref: " + transfer.getTransferId() + "]");
                        transfer.markFailed("Credit failed, amount refunded: " + ex.getMessage());
                        throw new RuntimeException("Transfer aborted and rolled back: " + ex.getMessage(), ex);
                    }
                } catch (Exception ex) {
                    if (transfer.getStatus() == TransferStatus.INITIATED) {
                        transfer.markFailed(ex.getMessage());
                    }
                    throw ex;
                }
            }
        }
        return transfer;
    }
}
