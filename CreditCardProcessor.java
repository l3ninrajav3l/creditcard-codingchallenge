import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Credit Card processing - Coding Challenge
 */
public class CreditCardProcessor {
    public static void main(String[] args) throws IOException {

        if (args.length > 0) {
			String fileName = args[0];
            System.out.println("File name : " + fileName);
            processCreditCards(fileName);
        } else {
            System.out.println(USAGE);
        }


    }

    static void processCreditCards(String fileName) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(fileName))
                .stream()
                .filter(line -> !line.startsWith(LINE_SEP))
                .collect(Collectors.toList());


        List<CreditCard> cards = lines.stream()
                .filter(line -> line.startsWith(TxType.Add.name()))
                .map(word -> word.split(WORDS_SEP))
                .map(Transactions::setUpCreditCards)
                .collect(Collectors.toList());


        List<Transaction> transactions = lines.stream()
                .filter(line -> !line.startsWith(TxType.Add.name()))
                .map(words -> words.split(WORDS_SEP))
                .map(Transactions::setUpTransactions)
                .collect(Collectors.toList());

        List<CreditCard> txCards = transactions.stream()
                .flatMap(tx -> cards.stream()
                        .filter(cc -> cc.getCardHolderName().equalsIgnoreCase(tx.getDoneBy()))
                        .map(creditCard -> creditCard.processTransaction(tx.getTxType(), tx.getTxAmount()))
                ).sorted()
                .distinct().collect(Collectors.toList());

       System.out.println(LINE_SEP);
       txCards.forEach(System.out::println);
       System.out.println(LINE_SEP);
    }

    private static final String LINE_SEP = "```";
    private static final String WORDS_SEP = " ";
    public static final String USAGE = "[*] Please provide input file name \n" +
            "[*] Example \n" +
            "[*] java CCP-APP I:\\cc.txt";

}

final class Transactions {

    private Transactions() {

    }

    static CreditCard setUpCreditCards(String[] fromString) {
        final CreditCard cc = CreditCard.of(fromString);
        return cc;
    }

    static Transaction setUpTransactions(String[] fromString) {
        final Transaction tx = Transaction.of(fromString);
        return tx;
    }
}


final class Transaction {

    Transaction(TxType type, String name, int amount) {
        txType = type;
        doneBy = name;
        txAmount = amount;
    }

    static Transaction of(String[] txValues) {
        final Transaction tx = new Transaction(TxType.valueOf(txValues[0]), txValues[1], Integer.parseInt(txValues[2].substring(1)));
        return tx;
    }

    String getDoneBy() {
        return doneBy;
    }

    int getTxAmount() {
        return txAmount;
    }

    TxType getTxType() {
        return txType;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "txType=" + txType +
                ", doneBy='" + doneBy + '\'' +
                ", txAmount=" + txAmount +
                '}';
    }

    private TxType txType;
    private String doneBy;
    private int txAmount;
}

enum TxType {

    Add, Charge, Credit
}

final class CreditCard implements Comparable<CreditCard> {

    CreditCard(String cardHolderName, String cardNumber, int txLimit) {
        this.cardHolderName = cardHolderName;
        this.cardNumber = cardNumber;
        this.txLimit = txLimit;
        balance = 0;
    }

    CreditCard processTransaction(TxType txType, int txAmount) {
        if (txType == TxType.Charge) {
            if (isLuhn10ValidCard(cardNumber) && (balance + txAmount < txLimit))
                balance += txAmount;
        } else if (txType == TxType.Credit) {
            if (isLuhn10ValidCard(cardNumber))
                balance -= txAmount;
        }
        return this;
    }

    static CreditCard of(String[] cardValues) {
        final CreditCard creditCard = new CreditCard(cardValues[1], cardValues[2], Integer.parseInt(cardValues[3].substring(1)));
        return creditCard;
    }

    CreditCard credit(int creditAmount) {
        if (isLuhn10ValidCard(cardNumber))
            balance -= creditAmount;
        return this;
    }

    String getCardHolderName() {
        return cardHolderName;
    }

    String getCardNumber() {
        return cardNumber;
    }

    int getTxLimit() {
        return txLimit;
    }

    int getBalance() {
        return balance;
    }

    String getBalanceSummary() {
        if (isLuhn10ValidCard(cardNumber)) {
            balanceSummary = "$" + balance;
        } else {
            balanceSummary = "error";
        }
        return balanceSummary;
    }

    boolean isLuhn10ValidCard(String ccNumber) {
        int sum = 0;
        boolean alternate = false;
        for (int i = ccNumber.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(ccNumber.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreditCard that = (CreditCard) o;
        return Objects.equals(cardHolderName, that.cardHolderName) &&
                Objects.equals(cardNumber, that.cardNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cardHolderName, cardNumber);
    }

    @Override
    public String toString() {
        return cardHolderName + ": " + getBalanceSummary();
    }

    private String cardHolderName;
    private String cardNumber;
    private int txLimit;
    private int balance;
    private String balanceSummary;


    @Override
    public int compareTo(CreditCard o) {
        return cardHolderName.compareTo(o.getCardHolderName());
    }
}
