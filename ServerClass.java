package task7;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerClass {
	private static final int SERVER_PORT = 4507;
	private static ArrayList<String> logins = new ArrayList<String>(5); //буду использовать массивы, пока не сделаю
	private static ArrayList<String> passwords = new ArrayList<String>(5); //запись/чтение файлов
	private static String message;
	public static void main(String[] args) throws IOException {
		final ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
		logins.add("Vlad");
		passwords.add("12345");
		try{
			while(true){
				final Socket socket = serverSocket.accept();
				try{
					final DataInputStream in = new DataInputStream(socket.getInputStream());
					final String work_type = in.readUTF();
					if(work_type.equals("CHECK_IN")){//режим проверки логина и парол€
						final String login = in.readUTF();
						final String password = in.readUTF();
						message = "false";
						for(int i=0; i<logins.size(); i++){
							if(login.equals(logins.get(i)) && password.equals(passwords.get(i))){
								message = "true";
								break;
							}
						}
					}
					final String port = in.readUTF();
					// ¬ыдел€ем IP-адрес
					final String address = ((InetSocketAddress)socket.getRemoteSocketAddress()).getAddress().getHostAddress();
					//открываю сокет дл€ отправки сообщений
					final Socket socket_out = new Socket(address, Integer.parseInt(port));
					final DataOutputStream out = new DataOutputStream(socket_out.getOutputStream());
					out.writeUTF(message);
					socket_out.close();
				}
				finally{
					socket.close();
				}
			}
		}
		finally{
			serverSocket.close();
		}

	}

}
