package org.aion.kernel;

import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.RuntimeAssertionError;
import org.aion.vm.api.interfaces.KernelInterface;
import org.aion.vm.api.interfaces.ResultCode;

import org.aion.vm.api.interfaces.TransactionResult;

public class AvmTransactionResult implements TransactionResult {

    private enum CodeType {
        SUCCESS, REJECTED, FAILED
    }

    public enum Code implements ResultCode {
        /**
         * The transaction was executed successfully.
         */
        SUCCESS(200, CodeType.SUCCESS),

        // rejected transactions should not be included on chain.

        /**
         * This transaction was rejected.
         */
        REJECTED(201, CodeType.REJECTED),

        /**
         * Insufficient balance to conduct the transaction.
         */
        REJECTED_INSUFFICIENT_BALANCE(202, CodeType.REJECTED),

        /**
         * The transaction nonce does not match the account nonce.
         */
        REJECTED_INVALID_NONCE(203, CodeType.REJECTED),

        // failed transaction can be included on chain, but energy charge will apply.

        /**
         * A failure occurred during the execution of the transaction.
         */
        FAILED(204, CodeType.FAILED),

        /**
         * The transaction data is malformed, for internal tx only.
         */
        FAILED_INVALID_DATA(205, CodeType.FAILED),

        /**
         * Transaction failed due to out of energy.
         */
        FAILED_OUT_OF_ENERGY(206, CodeType.FAILED),

        /**
         * Transaction failed due to stack overflow.
         */
        FAILED_OUT_OF_STACK(207, CodeType.FAILED),

        /**
         * Transaction failed due to exceeding the internal call depth limit.
         */
        FAILED_CALL_DEPTH_LIMIT_EXCEEDED(208, CodeType.FAILED),

        /**
         * Transaction failed due to a REVERT operation.
         */
        FAILED_REVERT(209, CodeType.FAILED),

        /**
         * Transaction failed due to an INVALID operation.
         */
        FAILED_INVALID(210, CodeType.FAILED),

        /**
         * Transaction failed due to an uncaught exception.
         */
        FAILED_EXCEPTION(211, CodeType.FAILED),

        /**
         * CREATE transaction failed due to a rejected of the user-provided classes.
         */
        FAILED_REJECTED(212, CodeType.FAILED),

        /**
         * Transaction failed due to an early abort.
         */
        FAILED_ABORT(213, CodeType.FAILED);

        private CodeType type;
        private int value;

        Code(int value, CodeType type) {
            this.type = type;
            this.value = value;
        }

        @Override
        public boolean isSuccess() {
            return type == CodeType.SUCCESS;
        }

        @Override
        public boolean isRejected() {
            return type == CodeType.REJECTED;
        }

        @Override
        public boolean isFailed() {
            return type == CodeType.FAILED;
        }

        @Override
        public boolean isRevert() {
            //TODO: confirm whether or not we want the same REVERT behaviour as the fvm.
            return false;
        }

        @Override
        public boolean isFatal() {
            // Avm currently has no fatal error codes defined yet.
            return false;
        }

        @Override
        public int toInt() {
            return value;
        }

    }

    /**
     * The energy limit of the transaction.
     */
    private final long energyLimit;

    /**
     * Any uncaught exception that flows to the AVM.
     */
    private Throwable uncaughtException;

    /**
     * The status code.
     */
    private ResultCode resultCode;

    /**
     * The return data.
     */
    private byte[] returnData;

    /**
     * The cumulative energy used.
     */
    private long energyUsed;

    /**
     * The amount of energy unused by the transaction.
     *
     * Note that we should always have: energyUsed + energyRemaining = energyLimit
     */
    private long energyRemaining;

    /**
     * The external transactional kernel generated by execution.
     */
    private KernelInterface kernel;

    /**
     * Constructs a result whose code is SUCCESS and whose energyUsed is as specified.
     */
    public AvmTransactionResult(long energyLimit, long energyUsed) {
        this.resultCode = Code.SUCCESS;
        this.energyLimit = energyLimit;
        this.energyUsed = energyUsed;
        this.energyRemaining = this.energyLimit - this.energyUsed;
    }

    @Override
    public ResultCode getResultCode() {
        return resultCode;
    }

    @Override
    public void setResultCode(ResultCode code) {
        resultCode = code;
    }

    @Override
    public byte[] getReturnData() {
        return returnData;
    }

    @Override
    public void setReturnData(byte[] returnData) {
        this.returnData = returnData;
    }

    @Override
    public long getEnergyRemaining() {
        return this.energyRemaining;
    }

    @Override
    public void setEnergyRemaining(long energyRemaining) {
        // In order to avoid double-setting this field (since it gets set by its dual 'energyUsed'
        // variable, we only allow it to be set in 'energyUsed'. Cuts down on the complexity.
        throw RuntimeAssertionError.unimplemented("This method should never be called.");
    }

    public long getEnergyUsed() {
        return energyUsed;
    }

    public void setEnergyUsed(long energyUsed) {
        this.energyUsed = energyUsed;
        this.energyRemaining = this.energyLimit - this.energyUsed;
    }

    public Throwable getUncaughtException() {
        return uncaughtException;
    }

    public void setUncaughtException(Throwable uncaughtException) {
        this.uncaughtException = uncaughtException;
    }

    @Override
    public void setKernelInterface(KernelInterface kernel) {
        this.kernel = kernel;
    }

    @Override
    public KernelInterface getKernelInterface() {
        return kernel;
    }

    @Override
    public String toString() {
        return "TransactionResult{" +
                "resultCode=" + resultCode +
                ", returnData=" + (returnData == null ? "NULL" : Helpers.bytesToHexString(returnData)) +
                ", energyUsed=" + energyUsed +
                '}';
    }

    @Override
    public byte[] toBytes() {
        throw new AssertionError("No equivalent concept in Avm exists for this.");
    }

}
