package org.aion.avm.core.util;

import avm.Address;
import avm.Result;
import a.ByteArray;
import i.IBlockchainRuntime;
import s.java.lang.String;

public class BlockchainRuntime {

    public static IBlockchainRuntime blockchainRuntime;

    private BlockchainRuntime() {
    }

    // Compiler-facing implementation.

    public static Address getAddress() {
        return new Address(blockchainRuntime.avm_getAddress().toByteArray());
    }

    public static Address getCaller() {
        return new Address(blockchainRuntime.avm_getCaller().toByteArray());
    }

    public static Address getOrigin() {
        return new Address(blockchainRuntime.avm_getOrigin().toByteArray());
    }

    public static long getEnergyLimit() {
        return blockchainRuntime.avm_getEnergyLimit();
    }

    public static long getEnergyPrice() {
        return blockchainRuntime.avm_getEnergyPrice();
    }

    public static java.math.BigInteger getValue() {
        return blockchainRuntime.avm_getValue().getUnderlying();
    }

    public static byte[] getData() {
        return blockchainRuntime.avm_getData().getUnderlying();
    }

    public static long getBlockTimestamp() {
        return blockchainRuntime.avm_getBlockTimestamp();
    }

    public static long getBlockNumber() {
        return blockchainRuntime.avm_getBlockNumber();
    }

    public static long getBlockEnergyLimit() {
        return blockchainRuntime.avm_getBlockEnergyLimit();
    }

    public static Address getBlockCoinbase() {
        return new Address(blockchainRuntime.avm_getBlockCoinbase().toByteArray());
    }

    public static java.math.BigInteger getBlockDifficulty() {
        return blockchainRuntime.avm_getBlockDifficulty().getUnderlying();
    }


    public static java.math.BigInteger getBalance(Address address) {
        return blockchainRuntime.avm_getBalance(new p.avm.Address(address.toByteArray())).getUnderlying();
    }

    public static java.math.BigInteger getBalanceOfThisContract() {
        return blockchainRuntime.avm_getBalanceOfThisContract().getUnderlying();
    }

    public static int getCodeSize(Address address) {
        return blockchainRuntime.avm_getCodeSize(new p.avm.Address(address.toByteArray()));
    }


    public static long getRemainingEnergy() {
        return blockchainRuntime.avm_getRemainingEnergy();
    }

    public static Result call(Address targetAddress, java.math.BigInteger value, byte[] data, long energyLimit) {
        //return blockchainRuntime.avm_call(new p.org.aion.avm.api.Address(targetAddress.toByteArray()), new BigInteger(value), new ByteArray(data), energyLimit);
        return new Result(true, new byte[0]);
    }

    public static Result create(java.math.BigInteger value, byte[] data, long energyLimit) {
        //return blockchainRuntime.avm_create(new BigInteger(value), new ByteArray(data), energyLimit);
        return new Result(true, new byte[0]);
    }

    public static void selfDestruct(Address beneficiary) {
        blockchainRuntime.avm_selfDestruct(new p.avm.Address(beneficiary.toByteArray()));
    }

    public static void log(byte[] data) {
        blockchainRuntime.avm_log(new ByteArray(data));
    }

    public static void log(byte[] topic1, byte[] data) {
        blockchainRuntime.avm_log(new ByteArray(topic1), new ByteArray(data));
    }

    public static void log(byte[] topic1, byte[] topic2, byte[] data) {
        blockchainRuntime.avm_log(new ByteArray(topic1), new ByteArray(topic2), new ByteArray(data));
    }

    public static void log(byte[] topic1, byte[] topic2, byte[] topic3, byte[] data) {
        blockchainRuntime.avm_log(new ByteArray(topic1), new ByteArray(topic2), new ByteArray(topic3), new ByteArray(data));
    }

    public static void log(byte[] topic1, byte[] topic2, byte[] topic3, byte[] topic4, byte[] data) {
        blockchainRuntime.avm_log(new ByteArray(topic1), new ByteArray(topic2), new ByteArray(topic3), new ByteArray(topic4), new ByteArray(data));
    }

    public static byte[] blake2b(byte[] data) {
        return blockchainRuntime.avm_blake2b(new ByteArray(data)).getUnderlying();
    }

    public static byte[] sha256(byte[] data) {
        return blockchainRuntime.avm_sha256(new ByteArray(data)).getUnderlying();
    }

    public static byte[] keccak256(byte[] data) {
        return blockchainRuntime.avm_keccak256(new ByteArray(data)).getUnderlying();
    }

    public static void revert() {
        blockchainRuntime.avm_revert();
    }

    public static void invalid() {
        blockchainRuntime.avm_invalid();
    }

    public static void require(boolean condition) {
        blockchainRuntime.avm_require(condition);
    }

    public static void print(java.lang.String message) {
        blockchainRuntime.avm_print(new String(message));
    }

    public static void println(java.lang.String message) {
        blockchainRuntime.avm_println(new String(message));
    }

    public static boolean edVerify(byte[] data, byte[] signature, byte[] publicKey) {
        return blockchainRuntime.avm_edVerify(new ByteArray(data), new ByteArray(signature), new ByteArray(publicKey));
    }
}
