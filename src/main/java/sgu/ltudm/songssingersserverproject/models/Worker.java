package sgu.ltudm.songssingersserverproject.models;

import sgu.ltudm.songssingersserverproject.encryptions.AESEncryption;
import sgu.ltudm.songssingersserverproject.encryptions.RSAEncryption;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class Worker implements Runnable {
    private final Socket socket;
    public SecretKey keyAES;
    private PublicKey publicKeyFromClient;
    private PrivateKey myPrivateKey;

    public Worker(Socket s) {
        this.socket = s;
    }

    private static PublicKey convertBytesToPublicKey(byte[] keyBytes) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA"); // Thay "RSA" bằng thuật toán sử dụng
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        return keyFactory.generatePublic(keySpec);
    }

    public void run() {
        System.out.println("Client == " + socket + " đã kết nối");
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            // khóa công khai của Server để cho Client nhận
            KeyPair keyPair = RSAEncryption.generateKeyPair();
            PublicKey publicKey = keyPair.getPublic();
            String myPublicKeyString = Base64.getEncoder().encodeToString(publicKey.getEncoded());
            myPrivateKey = keyPair.getPrivate();

            out.write(myPublicKeyString);
            out.newLine();
            out.flush();

            // khóa công khai của Client
            String publicKeyStringFromClient = in.readLine();
            publicKeyFromClient = convertBytesToPublicKey(Base64.getDecoder().decode(publicKeyStringFromClient));

            // lấy khóa đối xứng bị mã hóa, giải mã khóa đối xứng dùng khóa riêng tư của Server, ta được khóa đối xứng
            String keyAESEncodeStringFromClient = in.readLine();
            String keyAESString = RSAEncryption.decrypt(keyAESEncodeStringFromClient, myPrivateKey);
            keyAES = new SecretKeySpec(Base64.getDecoder().decode(keyAESString), "AES");

            String input = "";
            while (true) {
                try {
                    routing(in, out);
                    if (input.equals("bye")) break;
                } catch (Exception e) {
                    out.write(AESEncryption.encrypt("error:" + e.getMessage(), keyAES));
                    out.newLine();
                    out.flush();
                }
            }
            System.out.println("Closed socket for client " + socket.toString());
            in.close();
            out.close();
            socket.close();
        } catch (Exception e) {
            System.err.println("Lỗi của Client: " + e.getMessage());
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                    System.out.println("Closed socket for client " + socket);
                } catch (IOException e) {
                    System.err.println("Lỗi khi đóng kết nối với Client: " + e.getMessage());
                }
            }
        }
    }

    public void routing(BufferedReader in, BufferedWriter out) throws Exception {
        String dataDecode = AESEncryption.decrypt(in.readLine(), keyAES);

        String[] data = dataDecode.split(":");

        System.out.println("Request route: " + data[0]);

        switch (data[0]) {
            case "singer": {
                SingerServer singerServer = new SingerServer(keyAES);
                singerServer.requestSingerByName(data[1], out);
                break;
            }
            case "song": {
                SongServer songServer = new SongServer();
                songServer.setKeyAES(keyAES);
                songServer.requestSongsByTitle(data[1], out);
                break;
            }
        }
    }
}
