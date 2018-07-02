import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class Process {

    private static final long SPECIAL_USER = 2456938384156277127l;

    public static void main(String[] args) {
        List<TransactionData> transactions = new ArrayList<>();
        try {
            DataInputStream in = new DataInputStream(new FileInputStream("./txnlog.dat"));
            int numberRecords = readHeader(in);
            readTransactions(numberRecords, in, transactions);
            in.close();
            getTotalAmountOfDebits(transactions);
            getTotalAmountOfCredits(transactions);
            getNumberOfTimesAutoplayStarted(transactions);
            getNumberOfTimesAutoplayEnded(transactions);
            getBalanceForUser(transactions, SPECIAL_USER);
            transactions.clear();
        } catch (IOException ioe) {
            System.out.println(ioe);
        }
    }

    static int readHeader(DataInputStream in) throws IOException {
        byte magicString[] = new byte[4];
        magicString[0] = in.readByte();
        magicString[1] = in.readByte();
        magicString[2] = in.readByte();
        magicString[3] = in.readByte();
        String magic = new String(magicString);
        byte version = in.readByte();
        int numberRecords = in.readInt();
        return numberRecords;
    }

    static void readTransactions(int numberOfRecords, DataInputStream in, List<TransactionData> transactions)
            throws IOException {
        for (int i = 0; i < numberOfRecords; i++) {
            TransactionData transaction = new TransactionData();
            transaction.setRecordType(TransactionType.fromByte(in.readByte()));
            transaction.setTimestamp(in.readInt());
            transaction.setUserId(in.readLong());
            if (transaction.getRecordType() == TransactionType.DEBIT
                    || transaction.getRecordType() == TransactionType.CREDIT) {
                transaction.setAmount(in.readDouble());
            }
            transactions.add(transaction);
        }
    }

    static void getTotalAmountOfDebits(List<TransactionData> transactions) {
        double dollarTotal = 0.0;
        for (TransactionData transaction : transactions) {
            if (transaction.recordType == TransactionType.DEBIT) {
                dollarTotal += transaction.getAmount();
            }
        }
        System.out.println("Total Debits $" + dollarTotal);
    }

    static void getTotalAmountOfCredits(List<TransactionData> transactions) {
        double dollarTotal = 0.0;
        for (TransactionData transaction : transactions) {
            if (transaction.recordType == TransactionType.CREDIT) {
                dollarTotal += transaction.getAmount();
            }
        }
        System.out.println("Total Credits $" + dollarTotal);
    }

    static void getNumberOfTimesAutoplayStarted(List<TransactionData> transactions) {
        int timesStarted = 0;
        for (TransactionData transaction : transactions) {
            if (transaction.recordType == TransactionType.START_AUTOPLAY) {
                timesStarted++;
            }
        }
        System.out.println("Number of times Autoplay Started " + timesStarted);
    }

    static void getNumberOfTimesAutoplayEnded(List<TransactionData> transactions) {
        int timesEnded = 0;
        for (TransactionData transaction : transactions) {
            if (transaction.recordType == TransactionType.END_AUTOPLAY) {
                timesEnded++;
            }
        }
        System.out.println("Number of times Autoplay Ended " + timesEnded);
    }

    static void getBalanceForUser(List<TransactionData> transactions, long userId) {
        double balance = 0.0;
        for (TransactionData transaction : transactions) {
            if (transaction.getUserId() == userId) {
                System.out.println(transaction);
                if (transaction.recordType == TransactionType.DEBIT) {
                    balance -= transaction.getAmount();
                } else if (transaction.recordType == TransactionType.CREDIT) {
                    balance += transaction.getAmount();
                }
            }
        }
        System.out.println("Balance for user ID " + userId + " is $" + balance);
    }
}

class TransactionData {

    TransactionType recordType;

    int timestamp;

    long userId;

    double amount;

    public TransactionType getRecordType() {
        return recordType;
    }

    public void setRecordType(TransactionType recordType) {
        this.recordType = recordType;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "TransactionData [recordType=" + recordType + ", timestamp=" + timestamp + ", userId=" + userId
                + ", amount=" + amount + "]";
    }

}

enum TransactionType {
    DEBIT((byte) 0, "Debit"), 
    CREDIT((byte) 1, "Credit"), 
    START_AUTOPLAY((byte) 2, "Start autoplay"), 
    END_AUTOPLAY((byte) 3, "End autoplay");

    private final byte code;
    
    private final String name;

    private TransactionType(byte code, String name) {
        this.code = code;
        this.name = name;
    }

    static TransactionType fromByte(byte code) {
        switch (code) {
        case 0:
            return DEBIT;
        case 1:
            return CREDIT;
        case 2:
            return START_AUTOPLAY;
        case 3:
            return END_AUTOPLAY;
        default:
            throw new RuntimeException("Transaction Type undefined " + code);
        }
    }

}
