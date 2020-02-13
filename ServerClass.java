package task7;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class ServerClass {
	private static final int SERVER_PORT = 4506;
	private static ArrayList<String> logins = new ArrayList<String>(5);
	private static ArrayList<String> passwords = new ArrayList<String>(5);
	private static String message;
	
	private static void writeDatabase() throws IOException{ //функция записи записи в файл
		FileWriter output_logins = new FileWriter("Logins.txt");
		FileWriter output_passwords = new FileWriter("Passwords.txt");
		for(int i=0; i<logins.size(); i++){
			output_logins.write(logins.get(i)+'\n');
			output_passwords.write(passwords.get(i)+'\n');
		}
		output_logins.close();
		output_passwords.close();
	}
	
	private static void readDatabase() throws IOException{ //функция чтения файла
		FileReader input_logins = new FileReader("Logins.txt");
		FileReader input_passwords = new FileReader("Passwords.txt");
		Scanner scan_logins = new Scanner(input_logins);
		Scanner scan_passwords = new Scanner(input_passwords);
		while(scan_logins.hasNextLine()){
			logins.add(scan_logins.nextLine());
		}
		while(scan_passwords.hasNextLine()){
			passwords.add(scan_passwords.nextLine());
		}
		input_logins.close();
		input_passwords.close();
	}
	public static void main(String[] args) throws IOException {
		readDatabase();
		final ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
		try{
			while(true){
				final Socket socket = serverSocket.accept();
				try{
					final DataInputStream in = new DataInputStream(socket.getInputStream());
					final String work_type = in.readUTF(); //режим работы сервера на данной итерации
					if(work_type.equals("CHECK_IN")){//режим проверки логина и пароля
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
					else if(work_type.equals("NEW_USER")){ //режим регистрации новой уч.записи
						final String login = in.readUTF();
						final String password = in.readUTF();
						boolean repeated = false;
						for(int i=0; i<logins.size(); i++){
							if(login.equals(logins.get(i))){
								repeated = true;
								break;
							}
						}
						if(repeated == false){
							logins.add(login);
							passwords.add(password);
							message = "created";
							writeDatabase();
						}
						else{
							message = "not_created";
						}
					}
					final String port = in.readUTF();
					// Выделяем IP-адрес
					final String address = ((InetSocketAddress)socket.getRemoteSocketAddress()).getAddress().getHostAddress();
					//открываю сокет для отправки сообщений
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
