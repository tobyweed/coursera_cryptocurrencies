import java.util.ArrayList;

public class TxHandler {
    
    private UTXOPool utxoPool;
    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        this.utxoPool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        ArrayList<byte[]> claimedTxHashs = new ArrayList<byte[]>();
        double totalInputValue = 0;
        for (int i = 0; i < tx.numInputs(); i++) {
            Transaction.Input input = tx.getInput(i);
            UTXO claimedUtxo = new UTXO(input.prevTxHash, input.outputIndex);
            Transaction.Output claimedOutput = utxoPool.getTxOutput(claimedUtxo);
            byte[] inputHash = input.prevTxHash;
            
            //test 1
            if(!utxoPool.contains(claimedUtxo)) {
                return false;
            }
            //test 2
            if (!Crypto.verifySignature(claimedOutput.address, tx.getRawDataToSign(i), input.signature)){
                return false;
            }
            //test 3
            if(claimedTxHashs.contains(inputHash)){
                return false;
            }
            claimedTxHashs.add(inputHash);
            //test 5
            totalInputValue += claimedOutput.value;
            
        }
        //test 4
         ArrayList<Transaction.Output> outputs = new ArrayList<Transaction.Output>(tx.getOutputs());
         double totalOutputValue = 0;
        for( Transaction.Output output : outputs) {
             double value = output.value;
            if(value < 0) {
                return false;
            }
            totalOutputValue += value;
        }
        //test 5
        if(!(totalInputValue >= totalOutputValue)){
            return false;
        }
        return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
         ArrayList<Transaction> fTxs = new ArrayList<Transaction>();
        for(Transaction tx : possibleTxs) {
            if(isValidTx(tx)) {
                fTxs.add(tx);
                ArrayList<Transaction.Input> inputs = tx.getInputs();
                ArrayList<UTXO> spentUtxos = new ArrayList<UTXO>();
                for(Transaction.Input input : inputs) {
                     UTXO toAdd = new UTXO(input.prevTxHash, input.outputIndex);
                    spentUtxos.add(toAdd);
                }
                for(UTXO spentUtxo : spentUtxos){
                    utxoPool.removeUTXO(spentUtxo);
                }
            }
        }
        Transaction[] rTxs = fTxs.toArray(new Transaction[fTxs.size()]);
        return rTxs;
    }

}
