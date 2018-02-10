import java.util.ArrayList;

public class TxHandler {

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        this.uxtoPool = new UXTOPool(utxoPool);
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
        public ArrayList<Transaction.Input> inputs = new ArrayList<Transaction.Input>(tx.getInputs());
        public ArrayList<byte[]> claimedTxHashs = new ArrayList<byte[]>();
        public double totalInputValue = 0;
        for ( Transaction.Input input : inputs) {
            public byte[] inputHash = input.prevTxHash;
            public int index = input.outputIndex;
            
            //test 1
            utxo claimedUtxo = new UTXO(inputHash, index);
            if(!utxoPool.contains(claimedUtxo)) {
                return false;
            }
            //test 2
            Transaction.Output claimedOutput = utxoPool.getTxOutput(claimedUtxo);
            if(!claimedOutput || !verifySignature(claimedOutput.address,getRawDataToSign(index),input.signature)) {
                return false;
            }
            //test 3
            if(claimedTxHashs.contains(inputHash){
                return false;
            }
            claimedTxHashs.add(inputHash);
            //test 5
            totalInputValue += claimedOutput.value;
            
        }
        //test 4
        public ArrayList<Transaction.Output> outputs = new ArrayList<Transaction.Output>(tx.getOutputs());
        public double totalOutputValue = 0;
        for( Transaction.Output output : outputs) {
            public double value = output.value;
            if(value < 0) {
                return false;
            }
            totalOutputValue += value;
        }
        //test 5
        if(!totalInputValue >= totalOutputValue){
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
        public ArrayList<Transaction> fTxs = new ArrayList<Transaction>();
        for(Transaction tx : possibleTxs) {
            if(isValidTx(tx)) {
                fTxs.add(tx);
                public ArrayList<Transaction.Input> inputs = tx.getInputs();
                public ArrayList<UTXO> spentUtxos = new ArrayList<UTXO>();
                for(Transaction.Input input : inputs) {
                    public UTXO toAdd = new UTXO(input.prevTxHash, input.outputIndex);
                    spentUtxos.add(toAdd);
                }
                for(UTXO spentUtxo : spentUtxos){
                    uxtoPool.remove(spentUtxo);
                }
            }
        }
        Transaction[] rTxs = fTxs.toArray(new Transaction[fTxs.size()]);
        return rTxs;
    }

}
