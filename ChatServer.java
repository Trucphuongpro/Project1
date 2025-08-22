import java.io.*;
import java.net.*;
import java.util.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ChatServer {
    private static final int PORT = 12345;
    private static Map<String, Socket> users = new HashMap<>();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server đang chạy...");

        while (true) {
            Socket socket = serverSocket.accept();
            new Thread(() -> handleClient(socket)).start();
        }
    }

    private static void handleClient(Socket socket) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String username = in.readLine();     // đọc username
            String partnerName = in.readLine();  // đọc tên người muốn chat cùng

            if (username == null || partnerName == null) return;

            synchronized (users) {
                users.put(username, socket);
            }

            out.println("Kết nối thành công. Chờ người chat...");

            Socket partnerSocket = null;
            while (true) {
                synchronized (users) {
                    partnerSocket = users.get(partnerName);
                }
                if (partnerSocket != null && !partnerSocket.equals(socket)) break;
                Thread.sleep(500);
            }

            PrintWriter partnerOut = new PrintWriter(partnerSocket.getOutputStream(), true);
            out.println("Chat bắt đầu với " + partnerName);
            partnerOut.println("Chat bắt đầu với " + username);

            // Thêm định dạng thời gian
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

            while (true) {
                String msg = in.readLine();
                if (msg == null) break;
            
                String timestamp = "[" + LocalTime.now().format(formatter) + "]";
                String filteredMsg = callAI(msg);  // AI xử lý tin nhắn
                String fullMessage = timestamp + " " + username + ": " + filteredMsg;
            
                partnerOut.println(fullMessage);
            }
            
        } catch (Exception e) {
            System.out.println("Ngắt kết nối");
        }
    }
    public static String callAI(String message) {
        try {
            URL url = new URL("http://localhost:5000/analyze");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);
    
            String jsonInputString = "{\"message\": \"" + message + "\"}";
    
            try (OutputStream os = con.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
    
            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String responseLine;
    
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
    
            String json = response.toString();
            if (json.contains("\"toxic\":true")) {
                return "[Tin nhắn bị chặn bởi AI]";
            }
    
            int start = json.indexOf("\"reply\":\"") + 9;
            int end = json.indexOf("\"", start);
            return json.substring(start, end);
    
        } catch (Exception e) {
            System.out.println("Lỗi gọi AI: " + e.getMessage());
            return "[Lỗi AI]";
        }
    }
    
}
