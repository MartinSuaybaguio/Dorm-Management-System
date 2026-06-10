import java.io.*;

public class AccountManager {
    private static final String ACCOUNT_FILE = "accounts.txt";
    public static void saveAccount(String username, String password) {
        try (BufferedWriter bw =
                new BufferedWriter(
                        new FileWriter(ACCOUNT_FILE, true))) {

            bw.write(username + "," + password);
            bw.newLine();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean validateLogin(String username, String password) {
        try (BufferedReader br =
                new BufferedReader(
                        new FileReader(ACCOUNT_FILE))) {
            String line;

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");

                if (parts.length == 2 &&
                    parts[0].equals(username) &&
                    parts[1].equals(password)) {

                    return true;
                }
            }

        } catch (IOException e) {
            return false;
        }

        return false;
    }
}