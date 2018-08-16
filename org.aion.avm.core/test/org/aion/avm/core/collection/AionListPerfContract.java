package org.aion.avm.core.collection;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.userlib.AionList;

import java.util.List;

public class AionListPerfContract {

    public static int SIZE = 10000;

    public static AionList<Integer> target;

    static{
        target = new AionList<Integer>();
    }

    public static byte[] main() {
        return ABIDecoder.decodeAndRun(new AionListPerfContract(), BlockchainRuntime.getData());
    }

    public static void callInit(){
        for (int i = 0; i < SIZE; i++){
            target.add(Integer.valueOf(i));
        }
    }

    public static void callAppend(){
        target.add(Integer.valueOf(10));
    }

    public static void callInsertHead(){
        target.add(0, Integer.valueOf(10));
    }

    public static void callInsertMiddle(){
        target.add(SIZE/2, Integer.valueOf(10));
    }

}