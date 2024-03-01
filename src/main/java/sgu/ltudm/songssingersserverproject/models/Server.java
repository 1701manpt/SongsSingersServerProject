package sgu.ltudm.songssingersserverproject.models;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Server {
    public static int port = 1071;
    private static ServerSocket server = null;

    public static void main(String[] args) throws IOException {
        run();
    }

    public static void run() throws IOException {
        try {
            int corePoolSize = 2; // Số luồng tối thiểu trong pool
            int maximumPoolSize = 4; // Số luồng tối đa trong pool
            long keepAliveTime = 60; // Thời gian chờ trước khi luồng thừa bị loại bỏ
            TimeUnit unit = TimeUnit.SECONDS; // Đơn vị thời gian cho keepAliveTime
            // Sử dụng một BlockingQueue để quản lý các nhiệm vụ
            ArrayBlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(maximumPoolSize);
            ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);

            server = new ServerSocket(port);
            System.out.println("Server [addr = " + getIPAddressServer() + ", port = " + port + "] đang chạy...");
            postIPAddressToApi();

            while (true) {
                Socket socket = server.accept();
                executor.execute(new Worker(socket));
            }
        } catch (Exception e) {
            System.out.println("Lỗi của Server: " + e.getMessage());
        } finally {
            if (server != null) server.close();
        }
    }

    private static String getIPAddressServer() throws Exception {
        Socket socket1 = new Socket("google.com", 443);
        String addr = socket1.getLocalAddress().toString().substring(1);

        return addr;
    }

    private static void postIPAddressToApi() throws Exception {
        String addr = getIPAddressServer();

        // https://retoolapi.dev/fHdCgZ/ipAddressServer
        String api = "https://retoolapi.dev/fHdCgZ/ipAddressServer/1"; // Ghi vào dòng 1 trong DB
        String jsonData = "{\"ip\":\"" + addr + "\"}";
        Jsoup.connect(api).ignoreContentType(true).ignoreHttpErrors(true).header("Content-Type", "application/json").requestBody(jsonData).method(Connection.Method.PUT).execute();
    }

    public static void stop() throws IOException {
        server.close();
    }
}
