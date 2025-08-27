import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Chatclient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private JTextArea chatArea = new JTextArea();
    private JTextField inputField = new JTextField();

    public Chatclient(String host, int port, String username, String partner) throws Exception {
        socket = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        // Gửi username và partner tới server
        out.println(username);
        out.println(partner);

        // Giao diện
        JFrame frame = new JFrame("Chat: " + username);
        chatArea.setEditable(false);
        frame.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        frame.add(inputField, BorderLayout.SOUTH);
        frame.setSize(400, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        inputField.addActionListener(e -> {
            String msg = inputField.getText().trim();
            if (!msg.isEmpty()) {
                // Thêm thời gian client gửi vào trước khi gửi cho server (optional)
                String timestamp = "[" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + "]";
                out.println(msg); // chỉ gửi nội dung cho server
                inputField.setText("");
            }
        });

        // Nhận tin nhắn từ server
        new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    chatArea.append(line + "\n");
                }
            } catch (IOException e) {
                chatArea.append("Mất kết nối tới server\n");
            }
        }).start();
    }

    public static void main(String[] args) throws Exception {
        String server = JOptionPane.showInputDialog(null, "Nhập IP server (vd: localhost):", "Kết nối", JOptionPane.QUESTION_MESSAGE);
        String username = JOptionPane.showInputDialog(null, "Nhập username của bạn:", "Username", JOptionPane.PLAIN_MESSAGE);
        String partner = JOptionPane.showInputDialog(null, "Nhập tên người muốn chat cùng:", "Người nhận", JOptionPane.PLAIN_MESSAGE);

        if (server != null && username != null && partner != null) {
            new Chatclient(server.trim(), 12345, username.trim(), partner.trim());
        }
    }
    
}
