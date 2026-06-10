import java.awt.Component;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JPanel;

public class FileManager {

    private static final String FILE_PATH = "system_data.txt";
    private static final String UTILITIES_FILE = "utilities_data.txt";
    private static final String ROOM_FILE = "room_states.txt";

    public static void saveRecordToFile(String category, String data) {

        try (FileWriter fw = new FileWriter(FILE_PATH, true);
             BufferedWriter bw = new BufferedWriter(fw)) {

            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            bw.write("[" + timestamp + "] [" + category.toUpperCase() + "]");
            bw.newLine();
            bw.write(data);
            bw.write("------------------------------------------------");
            bw.newLine();

        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }

    public static void saveUtilityToFile(String category, String data) {

        try (FileWriter fw = new FileWriter(UTILITIES_FILE, true);
             BufferedWriter bw = new BufferedWriter(fw)) {

            bw.write("[" + category.toUpperCase() + "]");
            bw.newLine();

            bw.write(data);

            bw.write("------------------------------------------------");
            bw.newLine();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveRoomStates(JPanel gridPanel) {

        try (BufferedWriter bw =
                new BufferedWriter(
                        new FileWriter(ROOM_FILE, false))) {

            for (Component comp : gridPanel.getComponents()) {

                if (comp instanceof JButton) {

                    JButton btn = (JButton) comp;

                    bw.write(
                            btn.getText()
                            + ":"
                            + btn.getActionCommand());

                    bw.newLine();
                }
            }

        } catch (IOException e) {
            System.err.println(
                    "Error saving room states: "
                    + e.getMessage());
        }
    }

    public static ArrayList<String> loadRoomStates() {

        ArrayList<String> states = new ArrayList<>();

        try (BufferedReader br =
                new BufferedReader(
                        new FileReader(ROOM_FILE))) {

            String line;

            while ((line = br.readLine()) != null) {
                states.add(line);
            }

        } catch (IOException e) {
            System.err.println(
                    "Error loading room states: "
                    + e.getMessage());
        }

        return states;
    }
}