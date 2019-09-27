package org.aion.avm.core;

import java.math.BigInteger;

import org.aion.types.AionAddress;


/**
 * Defines the abstract behavioural requirements which must be provided by a consumer of the AVM.
 * These are provided from outside since they are either specific to a blockchain, not the AVM, or are generic
 * hashing or cryptographic utilities which the AVM need not duplicate.
 */
public interface IExternalCapabilities {
    /**
     * Computes the SHA-256 hash of the given data.
     * 
     * @param data The data to hash.
     * @return The SHA-256 hash of this data.
     */
    byte[] sha256(byte[] data);

    /**
     * Computes the BLAKE2B hash of the given data.
     * 
     * @param data The data to hash.
     * @return The BLAKE2B hash of this data.
     */
    byte[] blake2b(byte[] data);

    /**
     * Computes the KECCAK256 hash of the given data.
     * 
     * @param data The data to hash.
     * @return The KECCAK256 hash of this data.
     */
    byte[] keccak256(byte[] data);

    /**
     * Verifies that the given data, when signed by the private key counterpart to the given public key, produces the given signature.
     * 
     * @param data The data which was signed.
     * @param signature The signature produced.
     * @param publicKey The public key corresponding to the private key used to produce signature.
     * @return True if this public key verifies the signature.
     */
    boolean verifyEdDSA(byte[] data, byte[] signature, byte[] publicKey);

    /**
     * Determines the new contract address of a deployment issued by deployerAddress with the given nonce.
     * Note that this call must have NO SIDE-EFFECTS as it may be called multiple times on the same transaction.
     *
     * @param deployerAddress The address issuing the contract deployment.
     * @param nonce The nonce of the deployerAddress in the transaction where they are issuing the deployment.
     * @return The address of the new contract this transaction would create.
     */
    AionAddress generateContractAddress(AionAddress deployerAddress, BigInteger nonce);
}
